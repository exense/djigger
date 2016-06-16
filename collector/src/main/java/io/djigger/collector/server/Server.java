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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;

import io.djigger.client.Facade;
import io.djigger.client.FacadeListener;
import io.djigger.collector.accessors.InstrumentationEventAccessor;
import io.djigger.collector.accessors.MongoConnection;
import io.djigger.collector.accessors.ThreadInfoAccessor;
import io.djigger.collector.accessors.stackref.ThreadInfoAccessorImpl;
import io.djigger.collector.server.conf.CollectorConfig;
import io.djigger.collector.server.conf.Configurator;
import io.djigger.collector.server.conf.Connection;
import io.djigger.collector.server.conf.ConnectionGroupNode;
import io.djigger.collector.server.conf.ConnectionsConfig;
import io.djigger.collector.server.conf.MongoDBParameters;
import io.djigger.collector.server.services.ServiceServer;
import io.djigger.model.TaggedInstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.ThreadInfo;

public class Server {

	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	public static void main(String[] args) throws Exception {
		Server server = new Server();
		server.start();
	}
	
	private MongoConnection mongodbConnection;

	private ThreadInfoAccessor threadInfoAccessor;
	
	private InstrumentationEventAccessor instrumentationEventsAccessor;

	private List<ClientConnection> clients = new ArrayList<>();

	private ServiceServer serviceServer;

	public void start() throws Exception {
		try {

			String collConfigFilename = System.getProperty("collectorConfig");

			CollectorConfig config = Configurator.parseCollectorConfiguration(collConfigFilename);
			ConnectionsConfig cc = Configurator.parseConnectionsConfiguration(config.getConnectionFiles());

			initAccessors(config);

			processGroup(null, cc.getConnectionGroup());

			serviceServer = new ServiceServer(this);
			serviceServer.start(config.getServicePort()!=null?Integer.parseInt(config.getServicePort()):80);
		} catch (Exception e) {
			logger.error("A fatal error occurred while starting collector.",e);
			throw e;
		}
	}

	private void processGroup(Map<String,String> attributeStack, ConnectionGroupNode groupNode) {
		HashMap<String,String> attributes = new HashMap<>();
		if(attributeStack!=null) {
			attributes.putAll(attributeStack);
		}
		
		if(groupNode.getAttributes() != null && groupNode.getAttributes().size() > 0)
			attributes.putAll(groupNode.getAttributes());

		if(groupNode.getGroups()!=null) {
			for(ConnectionGroupNode child:groupNode.getGroups()) {
				processGroup(attributes, child);
			}
		}

		if(groupNode instanceof Connection) {
			Connection connectionParam = (Connection) groupNode;
			try {
				Facade client = createClient(attributes, connectionParam);
				synchronized (clients) {
					clients.add(new ClientConnection(client, attributes));
				}
			} catch (Exception e) {
				logger.error("An error occurred while creating client " + connectionParam.toString(), e);
			}
		}
	}

	private void initAccessors(CollectorConfig config) throws Exception {
		mongodbConnection = new MongoConnection();
		
		try {
			MongoDBParameters connectionParams = config.getDb();
			if(connectionParams.getPort()!=null) {
				Integer port = Integer.parseInt(connectionParams.getPort());
				mongodbConnection.connect(connectionParams.getHost(), port);				
			} else {
				mongodbConnection.connect(connectionParams.getHost());			
			}
			
			Long ttl = config.getDataTTL();
			
			threadInfoAccessor = new ThreadInfoAccessorImpl(mongodbConnection.getDb());
			threadInfoAccessor.createIndexesIfNeeded(ttl);
			
			instrumentationEventsAccessor = new InstrumentationEventAccessor(mongodbConnection.getDb());
			instrumentationEventsAccessor.createIndexesIfNeeded(ttl);
		} catch (MongoException e) {
			logger.error("An error occurred while connection to DB", e);
			throw e;
		}
	}

	private Facade createClient(final Map<String, String> attributes, Connection connectionConfig) throws Exception {
		Constructor<?> c = Class.forName(connectionConfig.getConnectionClass()).getDeclaredConstructor(Properties.class, boolean.class);
		final Facade client = (Facade) c.newInstance(connectionConfig.getConnectionProperties(), true);

		client.addListener(new FacadeListener() {

			@Override
			public void threadInfosReceived(List<ThreadInfo> threaddumps) {
				try {
					for(ThreadInfo dump:threaddumps) {
						dump.setAttributes(attributes);
						threadInfoAccessor.save(dump);
					}
				} catch(Exception e) {
					logger.error("An error occurred while saving dumps.",e);
				}
			}

			@Override
			public void instrumentationSamplesReceived(List<InstrumentationEvent> samples) {
				List<TaggedInstrumentationEvent> taggedEvents = new LinkedList<>();
				
				for(InstrumentationEvent event:samples) {
					boolean tagEvent = false;
					for(InstrumentSubscription subscription:client.getInstrumentationSubscriptions()) {
						if(subscription.getId()==event.getSubscriptionID()) {
							if(subscription.isTagEvent()) {
								tagEvent = true;
								break;
							}
						}
					}
					TaggedInstrumentationEvent  taggedEvent;
					if(tagEvent) {
						taggedEvent = new TaggedInstrumentationEvent(attributes, event);
					} else {
						taggedEvent = new TaggedInstrumentationEvent(null, event);
					}
					taggedEvents.add(taggedEvent);
				}
				instrumentationEventsAccessor.save(taggedEvents);
			}

			@Override
			public void connectionEstablished() {}

			@Override
			public void connectionClosed() {}
		});

		client.setSamplingInterval(connectionConfig.getSamplingParameters().getSamplingRate());
		client.setSampling(true);

		if(connectionConfig.getSubscriptions()!=null) {
			for(InstrumentSubscription subscription:connectionConfig.getSubscriptions()) {
				client.addInstrumentation(subscription);
			}
		}
		
		return client;
	}

	public List<ClientConnection> getClients() {
		synchronized (clients) {
			return new ArrayList<ClientConnection>(clients);
		}
	}
}
