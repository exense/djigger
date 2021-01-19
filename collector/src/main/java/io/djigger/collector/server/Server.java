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

import io.djigger.agent.InstrumentationError;
import io.djigger.client.Facade;
import io.djigger.client.FacadeListener;
import io.djigger.collector.accessors.*;
import io.djigger.collector.accessors.stackref.ThreadInfoAccessorImpl;
import io.djigger.collector.accessors.stackref.dbmodel.StackTraceEntry;
import io.djigger.collector.accessors.stackref.dbmodel.ThreadInfoEntry;
import io.djigger.collector.server.conf.*;
import io.djigger.model.TaggedInstrumentationEvent;
import io.djigger.model.TaggedMetric;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.model.ThreadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private ThreadDumpAccessor threadDumpAccessor;

    private StackTraceAccessor stackTraceAccessor;

    private InstrumentationEventAccessor instrumentationEventsAccessor;

    private MetricAccessor metricAccessor;

    private List<ClientConnection> clients = new ArrayList<>();

    private ServerContext context;

    public void start(ServerContext context) throws Exception {
        try {
            this.context = context;
            String collConfigFilename = context.getConfiguration().getProperty("io.djigger.collector.configFile","conf/Collector.xml");

            CollectorConfig config = Configurator.parseCollectorConfiguration(collConfigFilename);
            ConnectionsConfig cc = Configurator.parseConnectionsConfiguration(config.getConnectionFiles());

            //initAccessors(config);
            Long ttl = config.getDataTTL();

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

            processGroup(null, cc.getConnectionGroup());
        } catch (Exception e) {
            logger.error("A fatal error occurred while starting collector.", e);
            throw e;
        }
    }

    protected void processGroup(Map<String, String> attributeStack, ConnectionGroupNode groupNode) {
        HashMap<String, String> attributes = new HashMap<>();
        if (attributeStack != null) {
            attributes.putAll(attributeStack);
        }

        if (groupNode.getAttributes() != null && groupNode.getAttributes().size() > 0)
            attributes.putAll(groupNode.getAttributes());

        if (groupNode.getGroups() != null) {
            for (ConnectionGroupNode child : groupNode.getGroups()) {
                processGroup(attributes, child);
            }
        }

        if (groupNode instanceof Connection) {
            Connection connectionParam = (Connection) groupNode;
            try {
            	mergeSubscriptions(connectionParam);
                Facade client = createClient(attributes, connectionParam);
                synchronized (clients) {
                    clients.add(new ClientConnection(client, attributes));
                }
            } catch (Exception e) {
                logger.error("An error occurred while creating client " + connectionParam.toString(), e);
            }
        }
    }
    
    private void mergeSubscriptions(Connection connectionParam) throws Exception {
    	List<InstrumentSubscription> subsFromFile = Configurator.parseSubscriptionsFiles(connectionParam.getSubscriptionFiles());
    	if (subsFromFile != null) {
	    	if (connectionParam.getSubscriptions() != null) {
	    		connectionParam.getSubscriptions().addAll(subsFromFile);
	    	} else {
	    		connectionParam.setSubscriptions(subsFromFile);
	    	}
    	}
    }


    private Facade createClient(final Map<String, String> attributes, Connection connectionConfig) throws Exception {
        Constructor<?> c = Class.forName(connectionConfig.getConnectionClass()).getDeclaredConstructor(Properties.class, boolean.class);
        final Facade client = (Facade) c.newInstance(connectionConfig.getConnectionProperties(), true);
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

        client.setSamplingInterval(connectionConfig.getSamplingParameters().getSamplingRate());
        client.setSampling(true);

        if (connectionConfig.getSubscriptions() != null) {
            for (InstrumentSubscription subscription : connectionConfig.getSubscriptions()) {
                client.addInstrumentation(subscription);
            }
        }

        if (connectionConfig.getMetrics() != null) {
            client.setMetricCollectionConfiguration(connectionConfig.getMetrics());
        }

        return client;
    }

    public List<ClientConnection> getClients() {
        synchronized (clients) {
            return new ArrayList<ClientConnection>(clients);
        }
    }

    public ClientConnection getClientConnection(String id) {
        ClientConnection result = null;
        for (ClientConnection c:getClients()) {
            if (c.getFacade().getConnectionId().equals(id)) {
                result = c;
                break;
            }
        }
        return result;
    }
}
