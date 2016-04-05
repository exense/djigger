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
