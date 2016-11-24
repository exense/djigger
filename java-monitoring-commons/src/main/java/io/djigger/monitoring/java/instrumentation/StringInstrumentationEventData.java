package io.djigger.monitoring.java.instrumentation;

public class StringInstrumentationEventData extends InstrumentationEventData {
		
	String payload;

	public StringInstrumentationEventData (String payload) {
		super();
		this.payload = payload;
	}
	
	public StringInstrumentationEventData (String payload, Integer maxSize) {
		super();
		this.payload = truncate(payload, maxSize);
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	private String truncate(String payload, Integer maxSize) {
		return payload.substring(0, Math.min(maxSize, payload.length()));
	}

	@Override
	public String toString() {
		return payload;
	}
	
}
