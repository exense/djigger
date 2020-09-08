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
package io.djigger.db.client;

import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.djigger.collector.accessors.*;
import io.djigger.collector.accessors.stackref.ThreadInfoAccessorImpl;

public class StoreClient {

    ThreadInfoAccessor threadInfoAccessor;

    InstrumentationEventAccessor instrumentationAccessor;

    MetricAccessor metricAccessor;

    public void connect(String host, int port, String user, String password, String dbName) throws Exception {
        //Make sure we don't try to connect with credentials when user is ""
        if (user!=null && user.equals("")) {
            user=null;
        }
        MongoClientSession mongoClientSession = new MongoClientSession(host, port, user, password, 20, dbName);
        ThreadDumpAccessor threadDumpAccessor = new ThreadDumpAccessor(mongoClientSession);
        StackTraceAccessor stackTraceAccessor = new StackTraceAccessor(mongoClientSession);
        threadInfoAccessor = new ThreadInfoAccessorImpl(threadDumpAccessor, stackTraceAccessor);
        instrumentationAccessor = new InstrumentationEventAccessor(mongoClientSession);
        metricAccessor = new MetricAccessor(mongoClientSession);
    }

    public ThreadInfoAccessor getThreadInfoAccessor() {
        return threadInfoAccessor;
    }

    public InstrumentationEventAccessor getInstrumentationAccessor() {
        return instrumentationAccessor;
    }

    public MetricAccessor getMetricAccessor() {
        return metricAccessor;
    }
}
