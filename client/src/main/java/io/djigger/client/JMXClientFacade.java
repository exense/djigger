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
import static java.lang.management.ManagementFactory.getPlatformMXBeans;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
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
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.sampling.Sampler;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

public class JMXClientFacade extends Facade implements NotificationListener {
	
	private static final Logger logger = LoggerFactory.getLogger(JMXClientFacade.class);
	
	JMXConnector connector;
	
	volatile ThreadMXBean bean;
	
	volatile OperatingSystemMXBean operatingSystemBean;
	
	volatile List<MemoryPoolMXBean> memoryPoolBeans;
	
	volatile List<GarbageCollectorMXBean> garbageCollectorBeans;
	
	volatile MemoryMXBean memoryBean;
	
	boolean collectMetrics;
	
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
					
					if(collectMetrics) {
						long time = System.currentTimeMillis();
						List<Metric<?>> metrics = new ArrayList<>();
						for(MemoryPoolMXBean b:memoryPoolBeans) {
							MemoryUsage u =b.getCollectionUsage();
							if(u!=null) {
								metrics.add(new Metric<>(time, "JMX/MemoryPool/"+b.getName()+"/Used",u.getUsed()));
								metrics.add(new Metric<>(time, "JMX/MemoryPool/"+b.getName()+"/Max",u.getMax()));
							}
						}
						
						for(GarbageCollectorMXBean b:garbageCollectorBeans) {
							metrics.add(new Metric<>(time, "JMX/GarbageCollector/"+b.getName()+"/CollectionCount",b.getCollectionCount()));
							metrics.add(new Metric<>(time, "JMX/GarbageCollector/"+b.getName()+"/CollectionTime",b.getCollectionTime()));
						}
						
						double systemLoadAverage = operatingSystemBean.getSystemLoadAverage();
						metrics.add(new Metric<>(time, "JMX/OperatingSystem/SystemLoadAverage", systemLoadAverage));
						
						MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
						metrics.add(new Metric<>(time, "JMX/Memory/HeapMemoryUsage/Used",heapUsage.getUsed()));
						metrics.add(new Metric<>(time, "JMX/Memory/HeapMemoryUsage/Max",heapUsage.getMax()));
						
						MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
						metrics.add(new Metric<>(time, "JMX/Memory/NonHeapMemoryUsage/Used",nonHeapUsage.getUsed()));
						metrics.add(new Metric<>(time, "JMX/Memory/NonHeapMemoryUsage/Max",nonHeapUsage.getMax()));

						
						for(FacadeListener listener:listeners) {
							listener.metricsReceived(metrics);
						}
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
		
		collectMetrics = Boolean.parseBoolean(properties.getProperty("collectMetrics","true"));
		
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
		
		if(collectMetrics) {
			memoryBean = newPlatformMXBeanProxy(connection, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
			
			memoryPoolBeans = getPlatformMXBeans(connection, MemoryPoolMXBean.class);
			
			garbageCollectorBeans = getPlatformMXBeans(connection, GarbageCollectorMXBean.class);
			
			operatingSystemBean = newPlatformMXBeanProxy(connection, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
		}
	}

	@Override
	public boolean hasInstrumentationSupport() {
		return false;
	}
}