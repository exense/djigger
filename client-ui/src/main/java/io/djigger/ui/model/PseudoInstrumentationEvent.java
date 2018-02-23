package io.djigger.ui.model;

import io.djigger.monitoring.java.instrumentation.InstrumentationEventWithThreadInfo;

public class PseudoInstrumentationEvent extends InstrumentationEventWithThreadInfo {

	public PseudoInstrumentationEvent(String classname, String methodname) {
		super(classname, methodname);
	}

}
