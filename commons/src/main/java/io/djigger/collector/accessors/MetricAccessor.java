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
import io.djigger.model.TaggedMetric;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.mongodb.client.model.Filters.*;

import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.generic.AbstractCRUDAccessor;

public class MetricAccessor extends AbstractCRUDAccessor<TaggedMetric>{

    private static final String collectionName = "metrics";

    private static final Logger logger = LoggerFactory.getLogger(MetricAccessor.class);

    private static final Logger metricWriter = LoggerFactory.getLogger("MetricWriter");

    protected ObjectMapper mapper;

    public MetricAccessor(MongoClientSession mongoClientSession) {
        super(mongoClientSession, MetricAccessor.collectionName, TaggedMetric.class);
        mapper = new ObjectMapper();
    }

    public void createIndexesIfNeeded(Long ttl) {
        createOrUpdateTTLIndex(getMongoCollection(MetricAccessor.collectionName), "time", ttl);
    }

    @Override
    public TaggedMetric save(TaggedMetric event) {
        log(event);
        return super.save(event);
    }

    @Override
    public void save(Collection<? extends TaggedMetric> entities) {
        entities.forEach(e->log(e));
        if (entities.size()>0) {
            super.save(entities);
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

    public Spliterator<TaggedMetric> get(Bson filter, Date from, Date to) {
        String query = buildQuery(filter, from, to);
        return collection.find(query).as(TaggedMetric.class).spliterator();

    }

    private String buildQuery(Bson mongoQuery, Date from, Date to) {
        Bson result = and(gt("metric.time", from), lt("metric.time", to));
        if (mongoQuery != null) {
            result = and(mongoQuery, result);
        }
        BsonDocument bsonDocument = result.toBsonDocument(BsonDocument.class,this.mongoClientSession.getMongoDatabase().getCodecRegistry());
        return bsonDocument.toJson();
    }
}
