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

package io.djigger;

import io.djigger.monitoring.java.instrumentation.InstrumentationAttributes;
import io.djigger.monitoring.java.instrumentation.InstrumentationAttributesHolder;
import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;


public class Collector {

	private static LinkedBlockingQueue<InstrumentationSample> buffer = new LinkedBlockingQueue<InstrumentationSample>();

	private static ThreadLocal<UUID> contextID = new ThreadLocal<UUID>();

	public static void start(Object instance, String classname, String method) {
		
		UUID contextID = Collector.contextID.get();
		if(contextID == null) {
			contextID = UUID.randomUUID();
			Collector.contextID.set(contextID);
		}
	}

	public static void report(String classname, String method, long start, boolean[] attributes) {
		long end = System.currentTimeMillis();
		try {
			InstrumentationAttributesHolder attributesHolder = new InstrumentationAttributesHolder();
			if(InstrumentationAttributes.hasStacktrace(attributes)) {
				StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
				ThreadInfo info = new ThreadInfo(ThreadDumpHelper.toStackTraceElement(stackTrace));
				attributesHolder.setStacktrace(info);
			}
			attributesHolder.setThreadID(Thread.currentThread().getId());
			buffer.put(new InstrumentationSample(contextID.get(), classname, method, start, end, attributesHolder));
		} catch (Exception e) {
			System.out.println("Error while reporting sample from "+classname+"."+method);
			e.printStackTrace();
		}
	}

	public static InstrumentationSample poll() throws InterruptedException {
		return buffer.take();
	}

	public static void drainTo(Collection<InstrumentationSample> to) {
		buffer.drainTo(to);
	}

}
