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

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import io.djigger.collector.accessors.stackref.AbstractAccessor;
import io.djigger.model.TaggedMetric;
import io.djigger.monitoring.java.model.Metric;

public class MetricAccessor extends AbstractAccessor {

	private static final Logger logger = LoggerFactory.getLogger(MetricAccessor.class);
	
	private static final Logger metricWriter = LoggerFactory.getLogger("MetricWriter");
	
	MongoDatabase db;

	MongoCollection<Document> metricsCollection;

	public MetricAccessor(MongoDatabase db) {
		super();

		this.db = db;
		metricsCollection = db.getCollection("metrics");
	}

	public void createIndexesIfNeeded(Long ttl) {
		createOrUpdateTTLIndex(metricsCollection, "time", ttl);
	}

	public void save(TaggedMetric metric) {
		Document doc = toDocument(metric);
		log(doc);
		metricsCollection.insertOne(doc);
	}

	public Iterator<Metric<?>> get(Bson filter, Date from, Date to) {
		Bson query = buildQuery(filter, from, to);
		return query(query);
	}

	private Iterator<Metric<?>> query(Bson query) {
		final MongoCursor<Document> documents = metricsCollection.find(query).iterator();
		return new Iterator<Metric<?>>() {

			@Override
			public boolean hasNext() {
				return documents.hasNext();
			}

			@Override
			public Metric<?> next() {
				Document doc = documents.next();
				Metric<?> event = fromDocument(doc);
				return event;
			}
			
			@Override
			public void remove() {
				throw new RuntimeException("Not implemented");
			}
		};
	}

	private Bson buildQuery(Bson mongoQuery, Date from, Date to) {
		Bson result = and(gt("time", from), lt("time", to));
		if (mongoQuery != null) {
			result = and(mongoQuery, result);
		}
		return result;
	}

	public void save(List<TaggedMetric> metrics) {
		List<Document> documents = new ArrayList<>();
		for (TaggedMetric metric : metrics) {
			Document document = toDocument(metric);
			
			documents.add(document);
			
			log(document);
		}
		metricsCollection.insertMany(documents);
	}

	private void log(Document document) {
		if(metricWriter.isTraceEnabled()) {
			metricWriter.trace(document.toJson());
		}
	}

	private Document toDocument(TaggedMetric taggedMetric) {
		Document doc = new Document();
		Metric<?> metric = taggedMetric.getMetric();
		doc.append("name", metric.getName());
		doc.append("value", metric.getValue());
		doc.append("time", new Date(metric.getTime()));
		if (taggedMetric.getTags() != null) {
			doc.putAll(taggedMetric.getTags());
		}
		return doc;
	}
	
	private Metric<?> fromDocument(Document doc) {
		String name = doc.getString("name");
		Object value = doc.get("value");
		long time = doc.getDate("time").getTime();
		Metric<Object> metric = new Metric<Object>(time, name, value);
		return metric;
	}
}
