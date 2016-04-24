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

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;

import java.lang.Thread.State;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoException;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.IndexOptions;

import io.djigger.collector.accessors.ThreadInfoAccessor;
import io.djigger.collector.accessors.stackref.dbmodel.StackTraceElementEntry;
import io.djigger.collector.accessors.stackref.dbmodel.StackTraceEntry;
import io.djigger.monitoring.java.model.ThreadInfo;


public class ThreadInfoAccessorImpl implements ThreadInfoAccessor {
	
	MongoClient mongoClient;
	
	MongoCollection<Document> threadInfoCollection;
	
	MongoCollection<Document> stackTracesCollection;
			
	LRUCache<ObjectId, StackTraceEntry> stackTracesCache = new LRUCache<>(1000);
	
	public ThreadInfoAccessorImpl() {
		super();
	}

	public void start(String host, String collection) throws UnknownHostException, MongoException {
		Builder o = MongoClientOptions.builder().serverSelectionTimeout(3000);  
		mongoClient = new MongoClient(host, o.build());
		
		// call this method to check if the connection succeeded as the mongo client lazy loads the connection 
		mongoClient.getAddress();
		
		MongoDatabase db = mongoClient.getDatabase(collection);
		
		threadInfoCollection = db.getCollection("threaddumps");
		stackTracesCollection = db.getCollection("stacktraces");
	}
	
	public void close() {
		mongoClient.close();
	}

	public void createIndexesIfNeeded(Long ttl) {
		Document hashCodeIndex = getIndex(stackTracesCollection, "hashcode");
		if(hashCodeIndex==null) {
			stackTracesCollection.createIndex(new Document("hashcode",1));
		}
		
		Document ttlIndex = getIndex(threadInfoCollection, "timestamp");
		if(ttlIndex==null) {
			if(ttl!=null && ttl>0) {
				createTimestampIndexWithTTL(ttl);
			} else {
				createTimestampIndex();
			}
		} else {
			if(ttl!=null && ttl>0) {
				if(!ttlIndex.containsKey("expireAfterSeconds") || !ttlIndex.getLong("expireAfterSeconds").equals(ttl)) {
					dropIndex(ttlIndex);
					createTimestampIndexWithTTL(ttl);
				}
			} else {
				if(ttlIndex.containsKey("expireAfterSeconds")) {
					dropIndex(ttlIndex);
					createTimestampIndex();
				}
			}
		}
	}

	private void dropIndex(Document ttlIndex) {
		threadInfoCollection.dropIndex(ttlIndex.getString("name"));
	}

	private void createTimestampIndexWithTTL(Long ttl) {
		IndexOptions options = new IndexOptions();
		options.expireAfter(ttl, TimeUnit.SECONDS);
		createTimestampIndexWithOptions(options);
	}
	
	private void createTimestampIndex() {
		IndexOptions options = new IndexOptions();
		createTimestampIndexWithOptions(options);
	}
	
	private void createTimestampIndexWithOptions(IndexOptions options) {
		threadInfoCollection.createIndex(new Document("timestamp", 1), options);
	}
	
	private Document getIndex(MongoCollection<Document> collection,String indexName) {
		for(Document index:collection.listIndexes()) {
			Object o = index.get("key");
			if(o instanceof Document) {
				if(((Document)o).containsKey(indexName)) {
					return (Document) index;
				}
			}
		}
		return null;
	}
	
	private Bson buildQuery(Bson mongoQuery, Date from, Date to) {
		Bson result = and(gt("timestamp", from), lt("timestamp", to));
		if(mongoQuery!=null) {
			result = and(mongoQuery, result);
		}
		return result;
	}

	private static long COUNT_MAXTIME_SECONDS=30; 
	
	public long count(Bson mongoQuery, Date from, Date to) throws TimeoutException {
		mongoQuery = buildQuery(mongoQuery, from, to);
		
		CountOptions options = new CountOptions();
		options.maxTime(COUNT_MAXTIME_SECONDS, TimeUnit.SECONDS);
		
		try {
			return threadInfoCollection.count(mongoQuery, options);
		} catch(MongoExecutionTimeoutException e) {
			throw new TimeoutException("Count exceeded time limit");
		}
	}
	
	public Iterable<ThreadInfo> query(Bson mongoQuery, Date from, Date to) {
		final Bson query = buildQuery(mongoQuery, from, to);

		return new Iterable<ThreadInfo>() {
			
			@Override
			public Iterator<ThreadInfo> iterator() {
				final Iterator<Document> it = threadInfoCollection.find(query).iterator();
				return new Iterator<ThreadInfo>() {
					
					@Override
					public void remove() {}
					
					@Override
					public ThreadInfo next() {
						Document dbo = it.next();
						
						StackTraceEntry s;
						synchronized (stackTracesCache) {
							ObjectId stackTraceID = (ObjectId) dbo.get("stackTraceID");
							s = stackTracesCache.get(stackTraceID);
							if(s==null) {
								Document o = stackTracesCollection.find(eq("_id",stackTraceID)).first();
								StackTraceElementEntry[] stacktrace =  fromDBObject(o.get("stacktrace")); 
								s = new StackTraceEntry((ObjectId)o.get("_id"), stacktrace);
								s.setHashcode((int) o.get("hashcode"));
								stackTracesCache.put(stackTraceID, s);
							}
						}
						
												
						ThreadInfo info = new ThreadInfo(StackTraceElementEntry.fromEntries(s.getElements()));
						info.setTimestamp(dbo.getDate("timestamp").getTime());
						info.setName(dbo.getString("name"));
						info.setId(dbo.getLong("id"));
						info.setState(State.valueOf(dbo.getString("state")));

						Map<String, String> attributes = new HashMap<String, String>();
						for(String key:dbo.keySet()) {
							Object o = dbo.get(key);
							if(o instanceof String)
								attributes.put(key, (String)o);
						}
						info.setAttributes(attributes);
						return info;
					}
					
					@Override
					public boolean hasNext() {
						return it.hasNext();
					}
				};
			}
		};
	}
	
	public void save(ThreadInfo threadInfo) {
		StackTraceElementEntry[] entries = StackTraceElementEntry.toEntries(threadInfo.getStackTrace());
		int hashcode = Arrays.hashCode(entries);
		
		ObjectId id = null;
		for(Document entry:stackTracesCollection.find(new Document("hashcode", hashcode))) {
			if(Arrays.equals(fromDBObject(entry.get("stacktrace")),entries)) {
				id = (ObjectId) entry.get("_id");
			}
		}
		if(id==null) {
			id = new ObjectId();
			StackTraceEntry newStackTraceEntry = new StackTraceEntry(id, entries);
			newStackTraceEntry.setHashcode(hashcode);
					
			insertStackTraceEntry(newStackTraceEntry);
		}
		
		Document o = new Document();
		o.putAll(threadInfo.getAttributes());
		o.put("stackTraceID", id);
		o.put("id", threadInfo.getId());
		o.put("name", threadInfo.getName());
		o.put("state", threadInfo.getState());
		o.put("timestamp", new Date(threadInfo.getTimestamp()));
		o.put("state", threadInfo.getState().toString());
		threadInfoCollection.insertOne(o);
	}
	
	private static StackTraceElementEntry[] fromDBObject(Object o) {
		@SuppressWarnings("unchecked")
		List<List<Object>> l = (List<List<Object>>) o;
		StackTraceElementEntry[] s = new StackTraceElementEntry[l.size()];
		for(int i=0;i<l.size();i++) {
			List<Object> e = (List<Object>) l.get(i);
			s[i] = new StackTraceElementEntry((String)e.get(0), (String)e.get(1), (String)e.get(2), (int) e.get(3));
		}
		return s;
	}
	
	private void insertStackTraceEntry(StackTraceEntry entry) {
		Document o = new Document();
		
		ArrayList<Object> table = new ArrayList<>(entry.getElements().length);
		
		for(int i=0;i<entry.getElements().length;i++) {
			StackTraceElementEntry e = entry.getElements()[i];
			ArrayList<Object> node = new ArrayList<Object>();
			node.add(e.getDeclaringClass());
			node.add(e.getMethodName());
			node.add(e.getFileName());
			node.add(e.getLineNumber());
			table.add(node);
		}
		o.put("_id", entry.get_id());
		o.put("stacktrace", table);
		o.put("hashcode", entry.getHashcode());
		
		stackTracesCollection.insertOne(o);
	}
}
