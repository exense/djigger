package io.djigger.model;

import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

import java.util.List;

public class Subscription extends AbstractOrganizableObject {

	private InstrumentSubscription subscription;

	public Subscription() {
		super();
	}

	public Subscription(InstrumentSubscription s) {
		this.subscription = s;
	}

	public InstrumentSubscription getSubscription() {
		return subscription;
	}

	public void setSubscription(InstrumentSubscription subscription) {
		this.subscription = subscription;
	}
}
