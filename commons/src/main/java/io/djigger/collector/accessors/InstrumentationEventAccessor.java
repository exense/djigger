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

import java.util.*;

import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.generic.AbstractCRUDAccessor;
import ch.exense.commons.core.web.container.JacksonMapperProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jongo.Find;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.djigger.model.TaggedInstrumentationEvent;

public class InstrumentationEventAccessor extends AbstractCRUDAccessor<TaggedInstrumentationEvent> {

    private static final String collectionName = "instrumentation";

    private static final Logger logger = LoggerFactory.getLogger(InstrumentationEventAccessor.class);

    private static final Logger eventWriter = LoggerFactory.getLogger("InstrumentationEventWriter");

    protected ObjectMapper mapper;

    public InstrumentationEventAccessor(MongoClientSession mongoClientSession) {
        super(mongoClientSession, InstrumentationEventAccessor.collectionName, TaggedInstrumentationEvent.class);
        mapper = JacksonMapperProvider.createMapper();
    }

    public void createIndexesIfNeeded(Long ttl) {
        createOrUpdateTTLIndex(getMongoCollection(InstrumentationEventAccessor.collectionName), "event.start", ttl);
    }

    @Override
    public void save(Collection<? extends TaggedInstrumentationEvent> entities) {
        entities.forEach(e->log(e));
        super.save(entities);
    }

    @Override
    public TaggedInstrumentationEvent save(TaggedInstrumentationEvent event) {
        log(event);
        return super.save(event);
    }

    private void log(TaggedInstrumentationEvent event) {
        if (eventWriter.isTraceEnabled()) {
            String eventAsJson;
            try {
                eventAsJson = mapper.writeValueAsString(event);
                eventWriter.trace(eventAsJson);
            } catch (JsonProcessingException e) {
                logger.error("Error while logging instrumentation event", e);
            }
        }
    }

    public Spliterator<TaggedInstrumentationEvent> getByParentId(ObjectId parentId) {
        HashMap<String, String> attr = new HashMap<>();
        attr.put("parentid", parentId.toString());
        return findManyByAttributes(attr);
    }

    public Spliterator<TaggedInstrumentationEvent> getByTransactionId(UUID transactionIDd) {
        HashMap<String, String> attr = new HashMap<>();
        attr.put("trid", transactionIDd.toString());
        return findManyByAttributes(attr);
    }

    public Spliterator<TaggedInstrumentationEvent> get(Bson filter, Date from, Date to) {
        String query = buildQuery(filter, from, to);
        return collection.find(query).as(TaggedInstrumentationEvent.class).spliterator();
    }

    private String buildQuery(Bson mongoQuery, Date from, Date to) {
        Bson result = and(gt("event.start", from.getTime()), lt("event.start", to.getTime()));
        if (mongoQuery != null) {
            result = and(mongoQuery, result);
        }
        BsonDocument bsonDocument = result.toBsonDocument(BsonDocument.class,this.mongoClientSession.getMongoDatabase().getCodecRegistry());
        return bsonDocument.toJson();
    }

}


