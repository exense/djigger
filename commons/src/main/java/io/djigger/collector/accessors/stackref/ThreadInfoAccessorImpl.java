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
package io.djigger.collector.accessors.stackref;

import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.client.model.CountOptions;
import io.djigger.collector.accessors.StackTraceAccessor;
import io.djigger.collector.accessors.ThreadDumpAccessor;
import io.djigger.collector.accessors.ThreadInfoAccessor;
import io.djigger.collector.accessors.stackref.dbmodel.StackTraceElementEntry;
import io.djigger.collector.accessors.stackref.dbmodel.StackTraceEntry;
import io.djigger.collector.accessors.stackref.dbmodel.ThreadInfoEntry;
import io.djigger.monitoring.java.model.GlobalThreadId;
import io.djigger.monitoring.java.model.ThreadInfo;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static com.mongodb.client.model.Filters.*;


public class ThreadInfoAccessorImpl implements ThreadInfoAccessor {

    ThreadDumpAccessor threadDumpAccessor;
    StackTraceAccessor stackTraceAccessor;

    LRUCache<ObjectId, StackTraceEntry> stackTracesCache = new LRUCache<>(1000);

    public ThreadInfoAccessorImpl(ThreadDumpAccessor threadDumpAccessor, StackTraceAccessor stackTraceAccessor) {
        super();
        this.threadDumpAccessor = threadDumpAccessor;
        this.stackTraceAccessor = stackTraceAccessor;

    }

    private String buildQuery(Bson mongoQuery, Date from, Date to) {
        Bson result = and(gt("timestamp", from), lt("timestamp", to));
        if (mongoQuery != null) {
            result = and(mongoQuery, result);
        }
        BsonDocument bsonDocument = result.toBsonDocument(BsonDocument.class,threadDumpAccessor.getMongoClientSession().getMongoDatabase().getCodecRegistry());
        return bsonDocument.toJson();
    }

    public long count(Bson mongoQuery, Date from, Date to, long timeout, TimeUnit timeUnit) throws TimeoutException {
        String query = buildQuery(mongoQuery, from, to);

        CountOptions options = new CountOptions();
        options.maxTime(timeout, timeUnit);

        try {
            return threadDumpAccessor.count(query, options);
        } catch (MongoExecutionTimeoutException e) {
            throw new TimeoutException("Count exceeded time limit");
        }
    }

    public Iterable<ThreadInfo> query(Bson mongoQuery, Date from, Date to) {
        return new ThreadInfoIterable(buildQuery(mongoQuery, from, to));
    }
    

    public class ThreadInfoIterable implements Iterable<ThreadInfo> {

       final String query;

        public ThreadInfoIterable(String query) {
            super();
            this.query = query;
        }

        @Override
        public Iterator<ThreadInfo> iterator() {
            final Iterator<ThreadInfoEntry> it = threadDumpAccessor.getJongoCollection().find(query).as(ThreadInfoEntry.class).iterator();
            return new Iterator<ThreadInfo>() {

                @Override
                public void remove() {
                }

                @Override
                public ThreadInfo next() {
                    ThreadInfoEntry tInfo = it.next();
                    StackTraceEntry stackTraceEntry;
                    synchronized (stackTracesCache) {
                        ObjectId stackTraceID = tInfo.getStackTraceID();
                        stackTraceEntry = stackTracesCache.get(stackTraceID);
                        if (stackTraceEntry == null) {
                            stackTraceEntry = stackTraceAccessor.get(stackTraceID);
                            stackTracesCache.put(stackTraceEntry.getId(), stackTraceEntry);
                        }
                    }

                    ThreadInfo info = new ThreadInfo(StackTraceElementEntry.fromEntries(stackTraceEntry.getElements()));
                    info.setTimestamp(tInfo.getTimestamp().getTime());
                    info.setName(tInfo.getName());
                    GlobalThreadId globalThreadId = new GlobalThreadId(tInfo.getRid(), tInfo.getTid());
                    info.setGlobalId(globalThreadId);
                    info.setState(tInfo.getState());

                    if (tInfo.getTrid() != null && !tInfo.getTrid().equals("")) {
                        info.setTransactionID(UUID.fromString(tInfo.getTrid()));
                    }

                    info.setTags(tInfo.getTags());
                    return info;
                }

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }
            };
        }
    };

    public void save(ThreadInfo threadInfo) {

        StackTraceElementEntry[] entries = StackTraceElementEntry.toEntries(threadInfo.getStackTrace());
        int hashcode = Arrays.hashCode(entries);

        AtomicReference<ObjectId> lambdaId = new AtomicReference<ObjectId>();
        stackTraceAccessor.findManyByHashcode(hashcode).forEachRemaining(entry -> {

            if (Arrays.equals(entry.getElements(), entries)) {
                lambdaId.set((ObjectId) entry.getId());
            }
        });
        ObjectId id;
        if (lambdaId.get() == null) {
            id = new ObjectId();
            StackTraceEntry newStackTraceEntry = new StackTraceEntry(id, entries);
            newStackTraceEntry.setHashcode(hashcode);

            stackTraceAccessor.save(newStackTraceEntry);
        } else {
            id = lambdaId.get();
        }

        ThreadInfoEntry threadEntry = new ThreadInfoEntry(threadInfo, id);
        threadDumpAccessor.save(threadEntry);
    }
}
