package io.djigger.monitoring.java.instrumentation;

public class StringInstrumentationEventData extends InstrumentationEventData {
	
	private static int MAX_SIZE = 1024;
	
	String payload;

	public StringInstrumentationEventData (String payload) {
		super();
		this.payload = truncate(payload);
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = truncate(payload);
	}

	private String truncate(String payload) {
		return payload.substring(0, Math.min(MAX_SIZE, payload.length()));
	}

	@Override
	public String toString() {
		return payload;
	}
	
}
