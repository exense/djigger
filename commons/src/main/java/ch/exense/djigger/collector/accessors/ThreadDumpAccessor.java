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
package ch.exense.djigger.collector.accessors;

import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.generic.AbstractCRUDAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.CountOptions;
import io.djigger.collector.accessors.stackref.dbmodel.ThreadInfoEntry;
import io.djigger.model.TaggedMetric;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Spliterator;

import static com.mongodb.client.model.Filters.*;

public class ThreadDumpAccessor extends AbstractCRUDAccessor<ThreadInfoEntry>{

    private static final String collectionName = "threaddumps";

    private static final Logger logger = LoggerFactory.getLogger(ThreadDumpAccessor.class);

    protected ObjectMapper mapper;

    public ThreadDumpAccessor(MongoClientSession mongoClientSession) {
        super(mongoClientSession, ThreadDumpAccessor.collectionName, ThreadInfoEntry.class);
        mapper = new ObjectMapper();
    }

    public void createIndexesIfNeeded(Long ttl) {
        createOrUpdateTTLIndex(getMongoCollection(ThreadDumpAccessor.collectionName), "timestamp",ttl);
    }


    public Spliterator<TaggedMetric> get(Bson filter, Date from, Date to) {
        String query = buildQuery(filter, from, to);
        return collection.find(query).as(TaggedMetric.class).spliterator();

    }

    private String buildQuery(Bson mongoQuery, Date from, Date to) {
        Bson result = and(gt("timestamp", from), lt("timestamp", to));
        if (mongoQuery != null) {
            result = and(mongoQuery, result);
        }
        BsonDocument bsonDocument = result.toBsonDocument(BsonDocument.class,this.mongoClientSession.getMongoDatabase().getCodecRegistry());
        return bsonDocument.toJson();
    }

    public long count(String query, CountOptions options) {
        return collection.count(query,options);
    }

    public MongoCollection getJongoCollection() {
        return collection;
    }

    public MongoClientSession getMongoClientSession() {
        return mongoClientSession;
    }
}
