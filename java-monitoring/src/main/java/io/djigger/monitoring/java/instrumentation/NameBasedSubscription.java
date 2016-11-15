package io.djigger.monitoring.java.instrumentation;

public interface NameBasedSubscription {

	public boolean isRelatedToClass(String classname);
	
	public boolean isRelatedToMethod(String method);
}
