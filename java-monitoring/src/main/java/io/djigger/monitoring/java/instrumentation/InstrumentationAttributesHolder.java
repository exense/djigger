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
