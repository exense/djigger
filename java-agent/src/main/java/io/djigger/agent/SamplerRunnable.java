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

import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

public class SamplerRunnable implements Runnable {
	
	private final LinkedBlockingQueue<io.djigger.monitoring.java.model.ThreadInfo> buffer = new LinkedBlockingQueue<>();

	private ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
	
	public SamplerRunnable() {
		super();
	}

	@Override
	public void run() {
		ThreadInfo[] infos = mxBean.dumpAllThreads(false, false);					
		buffer.addAll(ThreadDumpHelper.toThreadDump(infos));
	}
	
	public void drainTo(Collection<io.djigger.monitoring.java.model.ThreadInfo> to) {
		buffer.drainTo(to);
	}
}
