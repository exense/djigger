package io.djigger.agent;

import java.io.Serializable;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

public class InstrumentationError implements Serializable {

	private static final long serialVersionUID = 439824919665537908L;

	private final InstrumentSubscription subscription;
	
	private final String classname;
	
	private final Throwable exception;

	public InstrumentationError(InstrumentSubscription subscription, String classname, Throwable exception) {
		super();
		this.subscription = subscription;
		this.classname = classname;
		this.exception = exception;
	}

	public InstrumentSubscription getSubscription() {
		return subscription;
	}

	public String getClassname() {
		return classname;
	}

	public Throwable getException() {
		return exception;
	}
}
