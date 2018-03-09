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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import io.djigger.collector.accessors.stackref.AbstractAccessor;
import io.djigger.model.TaggedMetric;
import io.djigger.monitoring.java.model.GenericObject;
import io.djigger.monitoring.java.model.Metric;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

public class MetricAccessor extends AbstractAccessor {

    private static final Logger logger = LoggerFactory.getLogger(MetricAccessor.class);

    private static final Logger metricWriter = LoggerFactory.getLogger("MetricWriter");

    protected ObjectMapper mapper;

    MongoConnection dbConnection;

    MongoCollection<Document> metricsCollection;

    public MetricAccessor(MongoConnection dbConnection) {
        super();
        this.dbConnection = dbConnection;
        metricsCollection = dbConnection.getDb().getCollection("metrics");
        mapper = new ObjectMapper();
    }

    public void createIndexesIfNeeded(Long ttl) {
        createOrUpdateTTLIndex(metricsCollection, "time", ttl);
    }

    public void save(TaggedMetric metric) {
        Document doc = toDocument(metric);
        metricsCollection.insertOne(doc);

        log(metric);
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

            log(metric);
        }
        if (documents.size() > 0) {
            metricsCollection.insertMany(documents);
        }
    }

    private void log(TaggedMetric metric) {
        if (metricWriter.isTraceEnabled()) {
            String metricAsJson;
            try {
                metricAsJson = mapper.writeValueAsString(metric);
                metricWriter.trace(metricAsJson);
            } catch (JsonProcessingException e) {
                logger.error("Error while logging metric", e);
            }
        }
    }

    private Document toDocument(TaggedMetric taggedMetric) {
        Document doc = new Document();
        Metric<?> metric = taggedMetric.getMetric();
        doc.append("name", metric.getName());
        if (metric.getValue() instanceof GenericObject) {
            doc.append("value", genericObjectToDocument((GenericObject) metric.getValue()));
        } else {
            doc.append("value", metric.getValue());
        }
        doc.append("time", new Date(metric.getTime()));
        if (taggedMetric.getTags() != null) {
            doc.putAll(taggedMetric.getTags());
        }
        return doc;
    }

    // replacing "." and "$" by their unicodes as they are invalid keys in BSON
    private String encodeKey(String key) {
        return key.replace("\\\\", "\\\\\\\\").replace("\\$", "\\\\u0024").replace(".", "\\\\u002e");
    }

    private String decodeKey(String key) {
        return key.replace("\\\\u002e", ".").replace("\\\\u0024", "\\$").replace("\\\\\\\\", "\\\\");
    }

    private Document genericObjectToDocument(GenericObject object) {
        Document doc = new Document();
        for (String key : object.keySet()) {
            String escapedKey = encodeKey(key);
            Object value = object.get(key);
            if (value instanceof GenericObject) {
                doc.append(escapedKey, genericObjectToDocument((GenericObject) value));
            } else {
                doc.append(escapedKey, value);
            }
        }
        return doc;
    }

    private GenericObject documentToGenericObject(Document document) {
        GenericObject object = new GenericObject();
        for (String escapedKey : document.keySet()) {
            String key = decodeKey(escapedKey);
            Object value = document.get(escapedKey);
            if (value instanceof Document) {
                object.put(key, documentToGenericObject((Document) value));
            } else {
                object.put(key, value);
            }
        }
        return object;
    }

    private Metric<?> fromDocument(Document doc) {
        String name = doc.getString("name");
        Object value = doc.get("value");
        long time = doc.getDate("time").getTime();
        Metric<Object> metric = new Metric<Object>(time, name, value);
        if (value instanceof Document) {
            metric.setValue(documentToGenericObject((Document) value));
        }
        return metric;
    }
}
