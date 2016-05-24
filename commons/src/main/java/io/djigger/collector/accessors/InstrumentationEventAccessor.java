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
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import io.djigger.collector.accessors.stackref.AbstractAccessor;
import io.djigger.model.TaggedInstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;

public class InstrumentationEventAccessor extends AbstractAccessor {

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
		instrumentationEventsCollection.insertOne(doc);
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
				InstrumentationEvent event = new InstrumentationEvent(doc.getString("class"), doc.getString("method"),
						doc.getDate("start").getTime(), doc.getLong("duration"));

				event.setId(doc.getObjectId("_id"));
				event.setParentID(doc.getObjectId("parentid"));
//				event.setTransactionID((UUID) doc.get("trid"));
				event.setTransactionID(UUID.fromString(doc.getString("trid")));

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
		if (mongoQuery != null) {
			result = and(mongoQuery, result);
		}
		return result;
	}

	public void save(List<TaggedInstrumentationEvent> events) {
		List<Document> documents = new ArrayList<>();
		for (TaggedInstrumentationEvent instrumentationEvent : events) {
			documents.add(toDocument(instrumentationEvent));
		}
		instrumentationEventsCollection.insertMany(documents);
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
		return doc;
	}
}
