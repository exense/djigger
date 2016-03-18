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
package io.djigger.monitoring.java.instrumentation;

import io.djigger.monitoring.java.model.ThreadInfo;

import java.io.Serializable;

public class InstrumentationAttributesHolder implements Serializable {
	
	private static final long serialVersionUID = 7398438089740207165L;

	private long threadID;
	
	private ThreadInfo threadInfo;

	public ThreadInfo getStacktrace() {
		return threadInfo;
	}

	public void setStacktrace(ThreadInfo threadInfo) {
		this.threadInfo = threadInfo;
	}

	public long getThreadID() {
		return threadID;
	}

	public void setThreadID(long threadID) {
		this.threadID = threadID;
	}
	
	

}
