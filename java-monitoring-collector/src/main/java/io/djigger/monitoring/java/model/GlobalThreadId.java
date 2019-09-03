package io.djigger.monitoring.java.model;

import java.io.Serializable;

/**
 * This class represents a unique ID of a Java Thread across multiple JVMs  
 *
 */
public class GlobalThreadId implements Serializable {
	
	private static final long serialVersionUID = 323933850917940644L;

	protected String runtimeId;
	
	protected long threadId;
	
	public GlobalThreadId(String runtimeId, long threadId) {
		super();
		this.runtimeId = runtimeId;
		this.threadId = threadId;
	}

	/**
	 * @return a unique ID of the JVM aka the runtime
	 */
	public String getRuntimeId() {
		return runtimeId;
	}

	public void setRuntimeId(String runtimeId) {
		this.runtimeId = runtimeId;
	}

	/**
	 * @return the java thread ID
	 */
	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((runtimeId == null) ? 0 : runtimeId.hashCode());
		result = prime * result + (int) (threadId ^ (threadId >>> 32));
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
		GlobalThreadId other = (GlobalThreadId) obj;
		if (runtimeId == null) {
			if (other.runtimeId != null)
				return false;
		} else if (!runtimeId.equals(other.runtimeId))
			return false;
		if (threadId != other.threadId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GlobalThreadId [runtimeId=" + runtimeId + ", threadId=" + threadId + "]";
	}

}