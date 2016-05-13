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
package io.djigger.monitoring.java.sampling;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;


public class ThreadDumpHelper {
	
	{
		System.out.println("init");
	}

	public static List<io.djigger.monitoring.java.model.ThreadInfo> toThreadDump(ThreadInfo[] threadInfos) {
		long timestamp = System.currentTimeMillis();
		List<io.djigger.monitoring.java.model.ThreadInfo> snapshots = new ArrayList<>();
		for (ThreadInfo info : threadInfos) {
			io.djigger.monitoring.java.model.ThreadInfo i = toThreadInfo(timestamp, info);
			snapshots.add(i);
		}
		return snapshots;	
	}

	public static io.djigger.monitoring.java.model.ThreadInfo toThreadInfo(long timestamp, ThreadInfo info) {
		io.djigger.monitoring.java.model.ThreadInfo i = new io.djigger.monitoring.java.model.ThreadInfo(toStackTraceElement(info.getStackTrace(),0));
		i.setTimestamp(timestamp);
		i.setState(info.getThreadState());
		i.setName(info.getThreadName());
		i.setId(info.getThreadId());
		return i;
	}
	
	public static io.djigger.monitoring.java.model.StackTraceElement[] toStackTraceElement(StackTraceElement[] stacktrace, int offset) {
		io.djigger.monitoring.java.model.StackTraceElement[] result = new io.djigger.monitoring.java.model.StackTraceElement[stacktrace.length-offset];
		for(int i=offset;i<stacktrace.length;i++) {
			StackTraceElement el = stacktrace[i];
			result[i-offset] = new io.djigger.monitoring.java.model.StackTraceElement(el.getClassName(), el.getMethodName(), el.getFileName(), el.getLineNumber());
		}
		return result;
	}
}
