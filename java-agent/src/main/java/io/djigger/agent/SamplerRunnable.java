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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.UUID;

import io.djigger.monitoring.eventqueue.EventQueue;
import io.djigger.monitoring.java.instrumentation.Transaction;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

public class SamplerRunnable implements Runnable {
	
	private final EventQueue<io.djigger.monitoring.java.model.ThreadInfo> threadInfoQueue;
	
	private ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
	
	public SamplerRunnable(EventQueue<io.djigger.monitoring.java.model.ThreadInfo> threadInfoQueue) {
		super();
		this.threadInfoQueue = threadInfoQueue;
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
	}
}
