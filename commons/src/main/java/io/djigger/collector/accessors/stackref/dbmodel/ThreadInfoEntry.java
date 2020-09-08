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
package io.djigger.collector.accessors.stackref.dbmodel;

import ch.exense.commons.core.model.accessors.AbstractIdentifiableObject;
import io.djigger.monitoring.java.model.ThreadInfo;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;

public class ThreadInfoEntry extends AbstractIdentifiableObject {

    private Date timestamp;

    private String rid;

    private long tid;//named id previously

    private String trid;

    private String name;

    private Thread.State state;

    private Map<String, String> attributes;

    private ObjectId stackTraceID;

    public ThreadInfoEntry() {
        super();
    }

    public ThreadInfoEntry(ThreadInfo threadInfo, ObjectId stackTraceID) {
        super();
        this.timestamp = new Date(threadInfo.getTimestamp());
        this.tid = threadInfo.getGlobalId().getThreadId();
        this.rid = threadInfo.getGlobalId().getRuntimeId();
        if (threadInfo.getTransactionID() != null) {
            this.trid = threadInfo.getTransactionID().toString();
        }
        this.name = threadInfo.getName();
        this.state = threadInfo.getState();
        this.attributes = threadInfo.getAttributes();
        this.stackTraceID = stackTraceID;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }
    public String getTrid() {
        return trid;
    }

    public void setTrid(String trid) {
       this.trid = trid;
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

    public ObjectId getStackTraceID() {
        return stackTraceID;
    }

    public void setStackTraceID(ObjectId stackTraceID) {
        this.stackTraceID = stackTraceID;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
