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
package io.djigger.monitoring.java.model;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class ThreadInfo implements Serializable {

    private long timestamp;

    private GlobalThreadId globalId;

    private String name;

    private Thread.State state;

    private Map<String, String> attributes;

    private StackTraceElement[] stackTrace;

    private UUID transactionID;

    public ThreadInfo(StackTraceElement[] stackTrace) {
        super();

        this.stackTrace = stackTrace;
    }

    public ThreadInfo(StackTraceElement[] stackTrace, GlobalThreadId globalId, long timestamp) {
        super();
        this.globalId = globalId;
        this.stackTrace = stackTrace;
        this.timestamp = timestamp;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

	public GlobalThreadId getGlobalId() {
		return globalId;
	}

	public void setGlobalId(GlobalThreadId globalId) {
		this.globalId = globalId;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Thread.State getState() {
        return state;
    }

    public void setState(Thread.State state) {
        this.state = state;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    public UUID getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(UUID transactionID) {
        this.transactionID = transactionID;
    }
}
