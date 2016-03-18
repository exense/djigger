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
package io.djigger.monitoring.java.sampling;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ThreadDumpHelper {

	public static List<io.djigger.monitoring.java.model.ThreadInfo> toThreadDump(ThreadInfo[] threadInfos) {
		long timestamp = System.currentTimeMillis();
		List<io.djigger.monitoring.java.model.ThreadInfo> snapshots = new ArrayList<>();
		for (ThreadInfo info : threadInfos) {
			io.djigger.monitoring.java.model.ThreadInfo i = new io.djigger.monitoring.java.model.ThreadInfo(toStackTraceElement(info.getStackTrace()));
			i.setTimestamp(new Date(timestamp));
			i.setState(info.getThreadState());
			i.setName(info.getThreadName());
			i.setId(info.getThreadId());
			snapshots.add(i);
			
		}
		
		return snapshots;	
	}
	
	public static io.djigger.monitoring.java.model.StackTraceElement[] toStackTraceElement(StackTraceElement[] stacktrace) {
		io.djigger.monitoring.java.model.StackTraceElement[] result = new io.djigger.monitoring.java.model.StackTraceElement[stacktrace.length];
		for(int i=0;i<stacktrace.length;i++) {
			StackTraceElement el = stacktrace[i];
			result[i] = new io.djigger.monitoring.java.model.StackTraceElement(el.getClassName(), el.getMethodName(), el.getFileName(), el.getLineNumber());
			
		}
		return result;
	}
}
