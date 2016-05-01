package io.djigger.collector.accessors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class InstrumentationEventAccessor {

	MongoDatabase db;
	
	MongoCollection<Document> instrumentationEventsCollection;
	
	public InstrumentationEventAccessor(MongoDatabase db) {
		super();
		
		this.db = db;
		instrumentationEventsCollection = db.getCollection("instrumentation");
	}
	 
	public void save(InstrumentationEvent event) {
		Document doc = toDocument(event);
		instrumentationEventsCollection.insertOne(doc);
	}
	
	public Iterator<InstrumentationEvent> get(Bson filter, Date from, Date to) {
		Bson query = buildQuery(filter, from, to);
		
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
				InstrumentationEvent event = new InstrumentationEvent(doc.getString("class"), 
						doc.getString("method"), doc.getDate("start").getTime(), doc.getLong("duration"));
				return event;
			}

			@Override
			public void remove() {
				throw new RuntimeException("Not implemented");
			}
		};
	}
	
	private Bson buildQuery(Bson mongoQuery, Date from, Date to) {
		Bson result = and(gt("start", from), lt("start", to));
		if(mongoQuery!=null) {
			result = and(mongoQuery, result);
		}
		return result;
	}

	
	public void save(List<InstrumentationEvent> events) {
		List<Document> documents = new ArrayList<>();
		for (InstrumentationEvent instrumentationEvent : events) {
			documents.add(toDocument(instrumentationEvent));
		}
		instrumentationEventsCollection.insertMany(documents);
	}

	private Document toDocument(InstrumentationEvent event) {
		Document doc = new Document();
		doc.append("class", event.getClassname());
		doc.append("method", event.getMethodname());
		doc.append("start", new Date(event.getStart()));
		doc.append("duration", event.getDuration());
		doc.append("threadid", event.getThreadID());
		return doc;
	}
}
