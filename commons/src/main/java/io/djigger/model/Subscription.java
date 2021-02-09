package io.djigger.model;

import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

import java.util.List;

public class Subscription extends AbstractOrganizableObject {

	private InstrumentSubscription subscription;

	private List<String> connectionsIds;

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

	public List<String> getConnectionsIds() {
		return connectionsIds;
	}

	public void setConnectionsIds(List<String> connectionsIds) {
		this.connectionsIds = connectionsIds;
	}
}
