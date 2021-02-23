package io.djigger.collector.server.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AbstractResponse {

	private ResponseStatus status;
	private String metaMessage;
	private Object payload;

	public AbstractResponse(ResponseStatus status, String metaMessage, Object payload) {
		this.status = status;
		this.metaMessage = metaMessage;
		this.payload = payload;
	}

	public enum ResponseStatus{
		SUCCESS("SUCCESS"), WARNING("WARNING"), ERROR("ERROR");

		String name;

		@JsonCreator
		ResponseStatus(@JsonProperty("status") String name) {
			this.name = name;
		}
		String getName() {
			return name;
		}

	}

	public ResponseStatus getStatus() {
		return status;
	}

	public void setStatus(ResponseStatus status) {
		this.status = status;
	}

	public String getMetaMessage() {
		return metaMessage;
	}

	public void setMetaMessage(String metaMessage) {
		this.metaMessage = metaMessage;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public String toString(){
		return "status=" + this.getStatus() + "; meta=" + this.getMetaMessage()+ "; payload=" + this.getPayload();
	}
}

