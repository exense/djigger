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

import org.bson.types.ObjectId;

import io.djigger.monitoring.java.model.GlobalThreadId;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


public class InstrumentationEvent implements Serializable {

    private static final long serialVersionUID = 347226760314494168L;

    private String id;

    private String parentID;

    private int subscriptionID;

    private String transactionID;

    private String classname;

    private String methodname;

    private long start;

    private long duration;
    
    private GlobalThreadId globalThreadId;

    private List<InstrumentationEventData> data;

    private transient long startNano;

    public InstrumentationEvent() {super();}

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

    public int getSubscriptionID() {
        return subscriptionID;
    }

    public void setSubscriptionID(int subscriptionID) {
        this.subscriptionID = subscriptionID;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return start + duration / 1000000;
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

    public GlobalThreadId getGlobalThreadId() {
		return globalThreadId;
	}

	public void setGlobalThreadId(GlobalThreadId globalThreadID) {
		this.globalThreadId = globalThreadID;
	}

	public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
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

    public List<InstrumentationEventData> getData() {
        return data;
    }

    public void setData(List<InstrumentationEventData> data) {
        this.data = data;
    }

    public synchronized void addData(InstrumentationEventData data) {
        if (this.data == null) {
            this.data = new LinkedList<InstrumentationEventData>();
        }
        this.data.add(data);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InstrumentationEvent [classname=" + classname + ", methodname=" + methodname + "]";
    }
}
