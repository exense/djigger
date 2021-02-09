package io.djigger.accessors;

import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.generic.AbstractCRUDAccessor;
import io.djigger.model.Subscription;


public class SubscriptionAccessor extends AbstractCRUDAccessor<Subscription> {
	public SubscriptionAccessor(MongoClientSession clientSession) {
		super(clientSession, "subscriptions", Subscription.class);
	}

	public void createIndexesIfNeeded(Long ttl) {
	}
}
