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

import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.generic.AbstractCRUDAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.djigger.collector.accessors.stackref.dbmodel.StackTraceEntry;
import io.djigger.model.TaggedInstrumentationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Spliterator;

public class StackTraceAccessor extends AbstractCRUDAccessor<StackTraceEntry>{

    private static final String collectionName = "stacktraces";

    private static final Logger logger = LoggerFactory.getLogger(StackTraceAccessor.class);

    private static final Logger metricWriter = LoggerFactory.getLogger("MetricWriter");

    protected ObjectMapper mapper;

    public StackTraceAccessor(MongoClientSession mongoClientSession) {
        super(mongoClientSession, StackTraceAccessor.collectionName, StackTraceEntry.class);
        mapper = new ObjectMapper();
    }

    public void createIndexesIfNeeded(Long ttl) {
        createOrUpdateIndex(getMongoCollection(StackTraceAccessor.collectionName), "hashcode");
    }

    public StackTraceEntry findByHashcode(int hashcode) {
        return collection.findOne("{hashcode: # }",hashcode).as(StackTraceEntry.class);
    }

    public Spliterator<StackTraceEntry> findManyByHashcode(int hashcode) {
        return collection.find("{hashcode: # }",hashcode).as(StackTraceEntry.class).spliterator();
    }
}
