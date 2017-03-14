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
package io.djigger.collector.accessors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bson.BsonArray;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import io.djigger.collector.accessors.stackref.AbstractAccessor;
import io.djigger.model.TaggedInstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventData;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventWithThreadInfo;
import io.djigger.monitoring.java.instrumentation.StringInstrumentationEventData;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;

public class InstrumentationEventAccessor extends AbstractAccessor {

	private static final Logger logger = LoggerFactory.getLogger(InstrumentationEventAccessor.class);
	
	private static final Logger eventWriter = LoggerFactory.getLogger("InstrumentationEventWriter");
	
	MongoDatabase db;

	MongoCollection<Document> instrumentationEventsCollection;

	public InstrumentationEventAccessor(MongoDatabase db) {
		super();

		this.db = db;
		instrumentationEventsCollection = db.getCollection("instrumentation");
	}

	public void createIndexesIfNeeded(Long ttl) {
		createOrUpdateTTLIndex(instrumentationEventsCollection, "start", ttl);
		createOrUpdateIndex(instrumentationEventsCollection, "tagged");
	}

	public void save(TaggedInstrumentationEvent event) {
		Document doc = toDocument(event);
		
		log(doc);
		
		instrumentationEventsCollection.insertOne(doc);
	}

	public Iterator<InstrumentationEvent> getByParentId(ObjectId parentId) {
		Bson filter = new Document("parentid", parentId);
		return query(filter);
	}
	
	public Iterator<InstrumentationEvent> getByTransactionId(UUID transactionIDd) {
		Bson filter = new Document("trid", transactionIDd.toString());
		return query(filter);
	}

	public Iterator<InstrumentationEvent> get(Bson filter, Date from, Date to) {
		Bson query = buildQuery(filter, from, to);

		return query(query);
	}
	
	public Iterator<InstrumentationEvent> getTaggedEvents(Bson filter, Date from, Date to) {
		Bson query = buildQuery(filter, from, to);
		//query = and(query,new Document("tagged", true));
		return query(query);
	}

	private Iterator<InstrumentationEvent> query(Bson query) {
		final MongoCursor<Document> documents = instrumentationEventsCollection.find(query).iterator();
		return new Iterator<InstrumentationEvent>() {

			@Override
			public boolean hasNext() {
				return documents.hasNext();
			}

			@Override
			public InstrumentationEvent next() {
				Document doc = documents.next();
				InstrumentationEvent event = fromDocument(doc);
				return event;
			}

			private InstrumentationEvent fromDocument(Document doc) {
				
				InstrumentationEvent event;
				if(doc.containsKey("stacktrace")) {
					event = new InstrumentationEventWithThreadInfo(doc.getString("class"), doc.getString("method"));
					
					StackTraceElement[] stacktrace = fromDBObject(doc.get("stacktrace"));
					ThreadInfo info = new ThreadInfo(stacktrace);
					((InstrumentationEventWithThreadInfo)event).setThreadInfo(info);
				} else {
					event = new InstrumentationEvent(doc.getString("class"), doc.getString("method"));
				}

				event.setStart(doc.getDate("start").getTime());
				event.setDuration(doc.getLong("duration"));
				event.setId(doc.getObjectId("_id"));
				event.setThreadID(doc.getLong("threadid"));
				event.setParentID(doc.getObjectId("parentid"));
//				event.setTransactionID((UUID) doc.get("trid"));
				event.setTransactionID(UUID.fromString(doc.getString("trid")));

				if(doc.containsKey("data")) {
					List<?> array = (List<?>) doc.get("data");
					List<InstrumentationEventData> list = new LinkedList<InstrumentationEventData>();
					for(Object value:array) {
						list.add(new StringInstrumentationEventData((String)value));
					}
					event.setData(list);
				}
				
				return event;
			}
			
			private StackTraceElement[] fromDBObject(Object o) {
				@SuppressWarnings("unchecked")
				List<List<Object>> l = (List<List<Object>>) o;
				StackTraceElement[] s = new StackTraceElement[l.size()];
				for(int i=0;i<l.size();i++) {
					List<Object> e = (List<Object>) l.get(i);
					s[i] = new StackTraceElement((String)e.get(0), (String)e.get(1), (String)e.get(2), (int) e.get(3));
				}
				return s;
			}

			@Override
			public void remove() {
				throw new RuntimeException("Not implemented");
			}
		};
	}

	private Bson buildQuery(Bson mongoQuery, Date from, Date to) {
		Bson result = and(gt("start", from), lt("start", to));
		if (mongoQuery != null) {
			result = and(mongoQuery, result);
		}
		return result;
	}

	public void save(List<TaggedInstrumentationEvent> events) {
		List<Document> documents = new ArrayList<>();
		for (TaggedInstrumentationEvent instrumentationEvent : events) {
			Document document = toDocument(instrumentationEvent);
			
			documents.add(document);
			
			log(document);
		}
		instrumentationEventsCollection.insertMany(documents);
	}

	private void log(Document document) {
		if(eventWriter.isTraceEnabled()) {
			eventWriter.trace(document.toJson());
		}
	}

	private Document toDocument(TaggedInstrumentationEvent taggedEvent) {
		Document doc = new Document();
		InstrumentationEvent event = taggedEvent.getEvent();
		doc.append("class", event.getClassname());
		doc.append("method", event.getMethodname());
		doc.append("start", new Date(event.getStart()));
		doc.append("duration", event.getDuration());
		doc.append("threadid", event.getThreadID());
		doc.append("trid", event.getTransactionID().toString());
		doc.append("_id", event.getId());
		doc.append("parentid", event.getParentID());
		if (taggedEvent.getTags() != null) {
			doc.append("tagged", true);
			doc.putAll(taggedEvent.getTags());
		}
		if(event instanceof InstrumentationEventWithThreadInfo) {
			List<Object> stacktrace = stackTraceAsTable(((InstrumentationEventWithThreadInfo)event).getThreadInfo());
			doc.append("stacktrace", stacktrace);
		}
		List<InstrumentationEventData> data = event.getData();
		if(data!=null) {
			BsonArray array = new BsonArray();
			for(InstrumentationEventData eventData:data) {
				if(eventData instanceof StringInstrumentationEventData) {
					array.add(new BsonString(((StringInstrumentationEventData)eventData).getPayload()));
				}				
			}
			doc.append("data", array);
		}
		
		return doc;
	}
	
	private static List<Object> stackTraceAsTable(ThreadInfo info) {
		StackTraceElement[] stacktrace = info.getStackTrace();
		ArrayList<Object> table = new ArrayList<>(stacktrace.length);
		
		for(int i=0;i<stacktrace.length;i++) {
			StackTraceElement e = stacktrace[i];
			ArrayList<Object> node = new ArrayList<Object>();
			node.add(e.getClassName());
			node.add(e.getMethodName());
			node.add(e.getFileName());
			node.add(e.getLineNumber());
			table.add(node);
		}
		return table;
	}
}
