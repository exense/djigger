/*******************************************************************************
 * (C) Copyright 2016 Jérôme Comte and Dorian Cransac
 *
 *  This file is part of djigger
 *
 *  djigger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  djigger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with djigger.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package io.djigger.collector.server;

import ch.exense.commons.core.web.container.ServerContext;

import io.djigger.accessors.*;
import io.djigger.agent.InstrumentationError;
import io.djigger.client.Facade;
import io.djigger.client.FacadeListener;
import io.djigger.model.*;
import io.djigger.accessors.stackref.ThreadInfoAccessorImpl;
import io.djigger.accessors.stackref.dbmodel.StackTraceEntry;
import io.djigger.accessors.stackref.dbmodel.ThreadInfoEntry;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.model.ThreadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private ThreadDumpAccessor threadDumpAccessor;

    private StackTraceAccessor stackTraceAccessor;

    private InstrumentationEventAccessor instrumentationEventsAccessor;

    private ConnectionAccessor connectionAccessor;

    private SubscriptionAccessor subscriptionAccessor;

    private MetricAccessor metricAccessor;

    private ClientConnectionManager ccMgr = new ClientConnectionManager();

    private ServerContext context;

    public void start(ServerContext context) throws Exception {
        try {
            this.context = context;
            context.put(Server.class, this);

            Long ttl = context.getConfiguration().getPropertyAsLong("db.data.ttl");
            logger.info("Data retention period configured with " + ttl + " seconds.");

            threadDumpAccessor = (ThreadDumpAccessor) context.get(ThreadInfoEntry.class.getName());
            threadDumpAccessor.createIndexesIfNeeded(ttl);

            stackTraceAccessor = (StackTraceAccessor) context.get(StackTraceEntry.class.getName());
            stackTraceAccessor.createIndexesIfNeeded(ttl);

            ThreadInfoAccessorImpl threadInfoAccessor = new ThreadInfoAccessorImpl(threadDumpAccessor, stackTraceAccessor);
            context.put(ThreadInfoAccessor.class, threadInfoAccessor);

            instrumentationEventsAccessor = (InstrumentationEventAccessor) context.get(TaggedInstrumentationEvent.class.getName());
            instrumentationEventsAccessor.createIndexesIfNeeded(ttl);

            metricAccessor = (MetricAccessor) context.get(TaggedMetric.class.getName());
            metricAccessor.createIndexesIfNeeded(ttl);


            connectionAccessor = (ConnectionAccessor) context.get(Connection.class.getName());
            connectionAccessor.createIndexesIfNeeded(ttl);

            subscriptionAccessor = (SubscriptionAccessor) context.get(Subscription.class.getName());
            subscriptionAccessor.createIndexesIfNeeded(ttl);

            loadConnections();

        } catch (Exception e) {
            logger.error("A fatal error occurred while starting collector.", e);
            throw e;
        }
    }

    public void loadConnections() throws Exception {
        Iterator<Connection> all = connectionAccessor.getAll();
        while (all.hasNext()) {
            Connection next = all.next();
            //TODO check/review
            //Avoid to recreate same clients when reloading from DB
            //All live updates should be performned on the facade and saved to DB at the same time
            //so this should only be called at startup and import of configurations
            if (!ccMgr.hasConnectionId(next.getId().toString())) {
                Facade client = createClient(next.getAttributes(), next);
                synchronized (ccMgr) {
                    ccMgr.addClient(new ClientConnection(client, next.getAttributes()));
                }
                if (ccMgr.singleConnection(client) || client.supportMultipleConnection()) {
                    client.initConnection();
                }
            }
        }
    }

    private Facade createClient(final Map<String, String> attributes, Connection connection) throws Exception {
        Constructor<?> c = Class.forName(connection.getConnectionClass()).getDeclaredConstructor(String.class, Properties.class, boolean.class);
        final Facade client = (Facade) c.newInstance(connection.getId().toString(),
                connection.getConnectionProperties(), true);
        final ThreadInfoAccessor threadInfoAccessor = context.get(ThreadInfoAccessor.class);

        client.addListener(new FacadeListener() {

            @Override
            public void threadInfosReceived(List<ThreadInfo> threaddumps) {
                try {
                    for (ThreadInfo dump : threaddumps) {
                        dump.setTags(attributes);
                        // enrich with the runtime ID. TODO: support agent side RuntimeID?
                        dump.getGlobalId().setRuntimeId(client.getConnectionId());
                        threadInfoAccessor.save(dump);
                    }
                } catch (Exception e) {
                    logger.error("An error occurred while saving dumps.", e);
                }
            }

            @Override
            public void instrumentationSamplesReceived(List<InstrumentationEvent> samples) {
                List<TaggedInstrumentationEvent> taggedEvents = new LinkedList<>();

                for (InstrumentationEvent event : samples) {
                    boolean tagEvent = false;
                    for (InstrumentSubscription subscription : client.getInstrumentationSubscriptions()) {
                        if (subscription.getId() == event.getSubscriptionID()) {
                            if (subscription.isTagEvent()) {
                                tagEvent = true;
                                break;
                            }
                        }
                    }
                 // enrich with the runtime ID. TODO: support agent side RuntimeID?
                    event.getGlobalThreadId().setRuntimeId(client.getConnectionId());
                    TaggedInstrumentationEvent taggedEvent;
                    if (tagEvent) {
                        taggedEvent = new TaggedInstrumentationEvent(attributes, event);
                    } else {
                        taggedEvent = new TaggedInstrumentationEvent(null, event);
                    }
                    taggedEvents.add(taggedEvent);
                }
                instrumentationEventsAccessor.save(taggedEvents);
            }

            @Override
            public void metricsReceived(List<Metric<?>> metrics) {
                List<TaggedMetric> taggedMetrics = new ArrayList<>();
                for (Metric<?> metric : metrics) {
                    taggedMetrics.add(new TaggedMetric(attributes, metric));
                }
                if (taggedMetrics.size()>0) {
                    metricAccessor.save(taggedMetrics);
                }
            }

            @Override
            public void connectionEstablished() {
            }

            @Override
            public void connectionClosed() {
            }

			@Override
			public void instrumentationErrorReceived(InstrumentationError error) {
				logger.warn("Error while applying subscription "+error.getSubscription()+" on class "+error.getClassname(), error.getException());
			}
        });

        client.setSamplingInterval(connection.getSamplingParameters().getSamplingRate());
        client.setSampling(true);

        if (connection.getSubscriptions() != null) {
            for (Subscription subscription : connection.getSubscriptions()) {
                client.addInstrumentation(subscription.getSubscription());
            }
        }

        if (connection.getMetrics() != null) {
            client.setMetricCollectionConfiguration(connection.getMetrics());
        }

        return client;
    }

    public List<ClientConnection> getClients() {
        synchronized (ccMgr) {
            return new ArrayList<ClientConnection>(ccMgr.getClients());
        }
    }

    public ClientConnection getClientConnection(String id) {
        return ccMgr.getConnectionById(id);
    }
}
