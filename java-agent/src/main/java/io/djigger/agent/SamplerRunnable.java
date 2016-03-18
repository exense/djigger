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
