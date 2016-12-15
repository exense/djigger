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
package io.djigger.agent;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.djigger.monitoring.eventqueue.EventQueue;
import io.djigger.monitoring.java.instrumentation.Transaction;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

public class SamplerRunnable implements Runnable {
	
	private final EventQueue<io.djigger.monitoring.java.model.ThreadInfo> threadInfoQueue;
	
	private final EventQueue<io.djigger.monitoring.java.model.Metric<?>> metricsQueue;
	
	private ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
	
	private List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
	
	private List<GarbageCollectorMXBean> garbageCollectorBeans = ManagementFactory.getGarbageCollectorMXBeans();
	
	private MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
	
	public SamplerRunnable(EventQueue<io.djigger.monitoring.java.model.ThreadInfo> threadInfoQueue, EventQueue<io.djigger.monitoring.java.model.Metric<?>> metricsQueue) {
		super();
		this.threadInfoQueue = threadInfoQueue;
		this.metricsQueue = metricsQueue;
	}

	@Override
	public void run() {
		ThreadInfo[] infos = mxBean.dumpAllThreads(false, false);
		long timestamp = System.currentTimeMillis();
		for (ThreadInfo threadInfo : infos) {
			io.djigger.monitoring.java.model.ThreadInfo event = ThreadDumpHelper.toThreadInfo(timestamp, threadInfo);
			Transaction currentTransaction = InstrumentationEventCollector.getCurrentTransaction(threadInfo.getThreadId());
			UUID currentTrID = currentTransaction!=null?currentTransaction.getId():null;
			event.setTransactionID(currentTrID);
			threadInfoQueue.add(event);
		}
		
		List<Metric<?>> metrics = new ArrayList<Metric<?>>();
		for(MemoryPoolMXBean b:memoryPoolBeans) {
			MemoryUsage u =b.getCollectionUsage();
			if(u!=null) {
				metrics.add(new Metric<Long>(timestamp, "JMX/MemoryPool/"+b.getName()+"/Used",u.getUsed()));
				metrics.add(new Metric<Long>(timestamp, "JMX/MemoryPool/"+b.getName()+"/Max",u.getMax()));
			}
		}
		
		for(GarbageCollectorMXBean b:garbageCollectorBeans) {
			metrics.add(new Metric<Long>(timestamp, "JMX/GarbageCollector/"+b.getName()+"/CollectionCount",b.getCollectionCount()));
			metrics.add(new Metric<Long>(timestamp, "JMX/GarbageCollector/"+b.getName()+"/CollectionTime",b.getCollectionTime()));
		}
		
		MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
		metrics.add(new Metric<Long>(timestamp, "JMX/Memory/HeapMemoryUsage/Used",heapUsage.getUsed()));
		metrics.add(new Metric<Long>(timestamp, "JMX/Memory/HeapMemoryUsage/Max",heapUsage.getMax()));
		
		MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
		metrics.add(new Metric<Long>(timestamp, "JMX/Memory/NonHeapMemoryUsage/Used",nonHeapUsage.getUsed()));
		metrics.add(new Metric<Long>(timestamp, "JMX/Memory/NonHeapMemoryUsage/Max",nonHeapUsage.getMax()));

		metricsQueue.add(metrics);
	}
}
