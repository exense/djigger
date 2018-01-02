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
package io.djigger.client;

import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
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

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.mbeans.MBeanCollector;
import io.djigger.monitoring.java.mbeans.MBeanCollector.ValueListener;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.sampling.Sampler;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

public class JMXClientFacade extends Facade implements NotificationListener {
	
	private static final Logger logger = LoggerFactory.getLogger(JMXClientFacade.class);
	
	protected JMXConnector connector;
	
	protected MBeanCollector mBeanCollector;
	
	protected volatile ThreadMXBean bean;
	
	protected final Sampler sampler;
	
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

					final List<Metric<?>> metrics = new ArrayList<>();
					
					mBeanCollector.collect(new ValueListener() {	
						@Override
						public void valueReceived(Metric<?> metric) {
							metrics.add(metric);
						}
					});
					
					for(FacadeListener listener:listeners) {
						listener.metricsReceived(metrics);
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
		
		mBeanCollector = new MBeanCollector(connection);
		if(metricCollectionConfiguration != null) {
			mBeanCollector.configure(metricCollectionConfiguration.getmBeans());
		}
	}

	@Override
	public boolean hasInstrumentationSupport() {
		return false;
	}
}