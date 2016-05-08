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

import java.io.Serializable;
import java.util.UUID;


public class InstrumentationEvent implements Serializable {

	private static final long serialVersionUID = 347226760314494168L;

	private String classname;
	
	private String methodname;
	
	private long start;

	private long duration;
	
	private long threadID;
	
	private UUID transactionID;
	
	private long localID; 
	
	private long localParentID;
	
	private transient long startNano;
	
	public InstrumentationEvent(String classname, String methodname) {
		super();
		this.classname = classname;
		this.methodname = methodname;
	}
	
	public InstrumentationEvent(String classname, String methodname, long start, long duration) {
		super();
		this.classname = classname;
		this.methodname = methodname;
		this.start = start;
		this.duration = duration;
	}

	public long getStart() {
		return start;
	}
	
	public long getEnd() {
		return start+duration/1000000;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getMethodname() {
		return methodname;
	}

	public void setMethodname(String methodname) {
		this.methodname = methodname;
	}
	
	public long getDuration() {
		return duration;
	}

	public long getThreadID() {
		return threadID;
	}

	public void setThreadID(long threadID) {
		this.threadID = threadID;
	}

	public UUID getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(UUID transactionID) {
		this.transactionID = transactionID;
	}

	public long getLocalID() {
		return localID;
	}

	public void setLocalID(long localID) {
		this.localID = localID;
	}

	public long getLocalParentID() {
		return localParentID;
	}

	public void setLocalParentID(long localParentID) {
		this.localParentID = localParentID;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getStartNano() {
		return startNano;
	}

	public void setStartNano(long startNano) {
		this.startNano = startNano;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (localID ^ (localID >>> 32));
		result = prime * result + ((transactionID == null) ? 0 : transactionID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InstrumentationEvent other = (InstrumentationEvent) obj;
		if (localID != other.localID)
			return false;
		if (transactionID == null) {
			if (other.transactionID != null)
				return false;
		} else if (!transactionID.equals(other.transactionID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InstrumentationEvent [classname=" + classname + ", methodname=" + methodname + "]";
	}
}
