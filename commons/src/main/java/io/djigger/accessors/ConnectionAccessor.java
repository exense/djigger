package io.djigger.accessors;

import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.generic.AbstractCRUDAccessor;

import io.djigger.model.Connection;


public class ConnectionAccessor extends AbstractCRUDAccessor<Connection> {

	public ConnectionAccessor(MongoClientSession clientSession) {
		super(clientSession, "connections", Connection.class);
	}

	public void createIndexesIfNeeded(Long ttl) {
	}
}
