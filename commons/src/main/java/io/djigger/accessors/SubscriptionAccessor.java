package io.djigger.accessors;

import ch.exense.commons.core.mongo.MixinDef;
import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.generic.AbstractCRUDAccessor;
import io.djigger.mixin.InstrumentSubscriptionMixin;
import io.djigger.model.Subscription;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

import java.util.ArrayList;
import java.util.List;


public class SubscriptionAccessor extends AbstractCRUDAccessor<Subscription> {
	static List<MixinDef> mixins = initMixins();
	static List<MixinDef> initMixins() {
		List<MixinDef> mixins = new ArrayList();
		MixinDef mixinDef = new MixinDef(InstrumentSubscription.class, InstrumentSubscriptionMixin.class);
		mixins.add(mixinDef);
		return mixins;
	}

	public SubscriptionAccessor(MongoClientSession clientSession) {
		super(clientSession, "subscriptions", Subscription.class, mixins);
	}

	public void createIndexesIfNeeded(Long ttl) {
	}
}
