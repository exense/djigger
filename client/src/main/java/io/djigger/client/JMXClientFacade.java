/*******************************************************************************
 * (C) Copyright  2016 Jérôme Comte and others.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *    - Jérôme Comte
 *    - Dorian Cransac (dcransac)
 *******************************************************************************/
package io.djigger.client;

import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.sampling.Sampler;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMXClientFacade extends Facade implements NotificationListener {
	
	private static final Logger logger = LoggerFactory.getLogger(JMXClientFacade.class);
	
	JMXConnector connector;
	
	volatile ThreadMXBean bean;
	
	final Sampler sampler;
	
	public JMXClientFacade(Properties properties, boolean autoReconnect) {
		super(properties, autoReconnect);
		
		final JMXClientFacade me = this;
		
		sampler = new Sampler(new Runnable() {
			@Override
			public void run() {
				if(me.isConnected() && bean!=null) {
					ThreadInfo[] infos = bean.dumpAllThreads(false, false);
					
					List<io.djigger.monitoring.java.model.ThreadInfo> dumps = ThreadDumpHelper.toThreadDump(infos);
					
					for(FacadeListener listener:listeners) {
						listener.threadInfosReceived(dumps);
					}
				}
			}
		});
		sampler.start();
	}

	@Override
	protected synchronized void destroy_() {
		sampler.destroy();

		if(connector!=null) {
			try {
				connector.close();
			} catch (IOException e) {}
		}
	}

	@Override
	public void handleNotification(Notification notification, Object handback) {
		if(notification.getType().equals("jmx.remote.connection.closed") && isConnected()) {
			handleConnectionClosed();
		}
	}


	@Override
	protected void addInstrumentation_(InstrumentSubscription subscription) {
		throw new RuntimeException("Not implemented");
	}


	@Override
	protected void removeInstrumentation_(InstrumentSubscription subscription) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	protected void startSampling() {
		sampler.setInterval(getSamplingInterval());
		sampler.setRun(true);
	}

	@Override
	protected void stopSampling() {
		sampler.setRun(false);
	}

	@Override
	protected synchronized void connect_() throws Exception {		
		String host = properties.getProperty("host");
		String port = properties.getProperty("port");
		String username = properties.getProperty("username");
		String password = properties.getProperty("password");
		
		logger.info("Creating JMX connection to " + host + ":" + port);
		
		String urlPath = "/jndi/rmi://" + host + ":" + port + "/jmxrmi";
		JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
			
		Hashtable<String, String[]> h  = new Hashtable<String, String[]>();
		if(username!=null) {
			String[] credentials = new String[] {username ,password }; 
			h.put("jmx.remote.credentials", credentials);
		}

		connector = JMXConnectorFactory.connect(url, h);
		connector.addConnectionNotificationListener(this, null, null);
		MBeanServerConnection connection = connector.getMBeanServerConnection();
		
		bean = newPlatformMXBeanProxy(connection, THREAD_MXBEAN_NAME, ThreadMXBean.class);
	}
}