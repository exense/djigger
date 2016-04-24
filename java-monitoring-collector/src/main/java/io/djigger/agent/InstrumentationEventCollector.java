package io.djigger.agent;

import io.djigger.monitoring.eventqueue.EventQueue;
import io.djigger.monitoring.java.instrumentation.InstrumentationAttributes;
import io.djigger.monitoring.java.instrumentation.InstrumentationAttributesHolder;
import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

public class InstrumentationEventCollector {

	private static EventQueue<InstrumentationSample> eventCollector;
	
	private static long tRef = System.currentTimeMillis();
	private static long tRefNano = System.nanoTime();
	
	private static long convertToTime(long tNano) {
		return (tNano-tRefNano)/1000000+tRef;
	}

	public static void setEventCollector(EventQueue<InstrumentationSample> eventCollector) {
		InstrumentationEventCollector.eventCollector = eventCollector;
	}

	public static void start(Object instance, String classname, String method) {
		

	}
	
	public static void report(String classname, String method, long startNano, long endNano, boolean[] attributes) {
		try {
			InstrumentationAttributesHolder attributesHolder = new InstrumentationAttributesHolder();
			if(InstrumentationAttributes.hasStacktrace(attributes)) {
				StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
				ThreadInfo info = new ThreadInfo(ThreadDumpHelper.toStackTraceElement(stackTrace,2));
				attributesHolder.setStacktrace(info);
			}
			attributesHolder.setThreadID(Thread.currentThread().getId());
			eventCollector.add(new InstrumentationSample(classname, method, convertToTime(startNano), endNano-startNano, attributesHolder));
		} catch (Exception e) {
			System.out.println("Error while reporting sample from "+classname+"."+method);
			e.printStackTrace();
		}
	}

}
