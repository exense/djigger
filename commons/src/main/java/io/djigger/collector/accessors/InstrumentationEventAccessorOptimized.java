package io.djigger.collector.accessors;

import static com.mongodb.client.model.Filters.eq;
import io.djigger.collector.accessors.stackref.LRUCache;
import io.djigger.collector.accessors.stackref.dbmodel.StackTraceElementEntry;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class InstrumentationEventAccessorOptimized {

	MongoDatabase db;
	
	MongoCollection<Document> instrumentationEventsCollection;
	
	MongoCollection<Document> stackTraceElementsCollection;
	
	LRUCache<ObjectId, StackTraceElementEntry> stackTraceElementCache = new LRUCache<>(1000);
	
	LRUCache<StackTraceElementEntry, ObjectId> reverseStackTraceElementCache = new LRUCache<>(1000);
	
	public InstrumentationEventAccessorOptimized(MongoDatabase db) {
		super();
		
		this.db = db;
		instrumentationEventsCollection = db.getCollection("instrumentation");
		stackTraceElementsCollection = db.getCollection("stackTraceElements");
	}
	 
	public void save(InstrumentationEvent event) {
		Document doc = toDocument(event);
		instrumentationEventsCollection.insertOne(doc);
	}
	
	public Iterator<InstrumentationEvent> get(Bson filter) {
		final MongoCursor<Document> documents = instrumentationEventsCollection.find(filter).iterator();
		return new Iterator<InstrumentationEvent>() {

			@Override
			public boolean hasNext() {
				
				
				return documents.hasNext();
			}

			@Override
			public InstrumentationEvent next() {
				Document doc = documents.next();
				
				ObjectId stackTraceElementID = (ObjectId) doc.get("nodeID");
				StackTraceElementEntry entry = stackTraceElementCache.get(stackTraceElementID);
				if(entry==null) {
					Document o = stackTraceElementsCollection.find(eq("_id",stackTraceElementID)).first();
					
					List<Object> e = (List<Object>) o.get("e");
					entry = new StackTraceElementEntry((String)e.get(0), (String)e.get(1), (String)e.get(2), (int) e.get(3));
					
					stackTraceElementCache.put(stackTraceElementID, entry);
				}

				InstrumentationEvent event = fromDocument(doc, entry);
				return event;
			}

			private InstrumentationEvent fromDocument(Document doc, StackTraceElementEntry entry) {
				InstrumentationEvent event = new InstrumentationEvent(entry.getDeclaringClass(), 
						entry.getMethodName(), doc.getDate("start").getTime(), doc.getLong("duration"));
				return event;
			}
			
//			private InstrumentationEvent fromDocument(Document doc, StackTraceElementEntry entry) {
//				InstrumentationEvent event = new InstrumentationEvent(doc.getString("class"), 
//						doc.getString("method"), doc.getDate("start").getTime(), doc.getLong("duration"));
//				return event;
//			}

			@Override
			public void remove() {
				throw new RuntimeException("Not implemented");
			}
		};
	}
	
//	public void save(List<InstrumentationEvent> events) {
//		List<Document> documents = new ArrayList<>();
//		for (InstrumentationEvent instrumentationEvent : events) {
//			documents.add(toDocument(instrumentationEvent));
//		}
//		instrumentationEventsCollection.insertMany(documents);
//	}
	
	public void save(List<InstrumentationEvent> events) {
		List<Document> documents = new ArrayList<>();
		for (InstrumentationEvent instrumentationEvent : events) {
			Document doc = toDocument(instrumentationEvent);
			documents.add(doc);
		}
		instrumentationEventsCollection.insertMany(documents);
	}

	private Document toDocument(InstrumentationEvent instrumentationEvent) {
		StackTraceElementEntry entry = new StackTraceElementEntry(instrumentationEvent.getClassname(), instrumentationEvent.getMethodname(),null, 0);
		
		ObjectId ref = getRefForNode(entry);
		
		Document doc = toDocument(instrumentationEvent, ref);
		return doc;
	}
	
	private ObjectId getRefForNode(StackTraceElementEntry entry) {
		ObjectId ref = findRefInCache(entry);
		
		if(ref==null) {
			int hashcode = entry.hashCode();
			
			ref = findRefInDb(entry, hashcode);
			
			if(ref==null) {
				ref = createAndInsertRef(entry, hashcode);
			}
			putRefInCache(entry, ref);
		}
		return ref;
	}
	
	private void putRefInCache(StackTraceElementEntry entry, ObjectId ref) {
		reverseStackTraceElementCache.put(entry, ref);
	}
	
	private ObjectId createAndInsertRef(StackTraceElementEntry entry, int hashcode) {
		ObjectId ref;
		ref = new ObjectId();
	
		Document doc = new Document();
		doc.put("_id", ref);
		
		ArrayList<Object> node = new ArrayList<Object>();
		node.add(entry.getDeclaringClass());
		node.add(entry.getMethodName());
		node.add(entry.getFileName());
		node.add(entry.getLineNumber());
		doc.put("e", node);
		
		doc.put("hashcode", hashcode);
		
		stackTraceElementsCollection.insertOne(doc);
		return ref;
	}
	
	private ObjectId findRefInCache(StackTraceElementEntry entry) {
		return reverseStackTraceElementCache.get(entry);
	}
	
	private ObjectId findRefInDb(StackTraceElementEntry entry, int hashcode) {
		ObjectId ref = null;
		for(Document doc:stackTraceElementsCollection.find(new Document("hashcode", hashcode))) {
			List<Object> e = (List<Object>) doc.get("e");
			StackTraceElementEntry el = new StackTraceElementEntry((String)e.get(0), (String)e.get(1), (String)e.get(2), (int) e.get(3));
			
			if(el.equals(entry)) {
				ref = doc.getObjectId("_id");
			}					
		}
		return ref;
	}

//	private Document toDocument(InstrumentationEvent event) {
//		Document doc = new Document();
//		doc.append("class", event.getClassname());
//		doc.append("method", event.getMethodname());
//		doc.append("start", new Date(event.getStart()));
//		doc.append("duration", event.getDuration());
//		return doc;
//	}
	
	private Document toDocument(InstrumentationEvent event, ObjectId ref) {
		Document doc = new Document();
		doc.append("ref", ref);
		doc.append("start", new Date(event.getStart()));
		doc.append("duration", event.getDuration());
		return doc;
	}
}
