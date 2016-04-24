package io.djigger.agent;

import io.djigger.monitoring.eventqueue.EventQueue;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventWithThreadInfo;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

public class InstrumentationEventCollector {

	private static EventQueue<InstrumentationEvent> eventCollector;
	
	private static long tRef = System.currentTimeMillis();
	private static long tRefNano = System.nanoTime();
	
	private static long convertToTime(long tNano) {
		return (tNano-tRefNano)/1000000+tRef;
	}

	public static void setEventCollector(EventQueue<InstrumentationEvent> eventCollector) {
		InstrumentationEventCollector.eventCollector = eventCollector;
	}

	public static void start(Object instance, String classname, String method) {
		

	}
	
	public static void report(String classname, String method, long startNano, long endNano) {
		try {
			InstrumentationEvent event = new InstrumentationEvent(classname, method, convertToTime(startNano), endNano-startNano);
			event.setThreadID(Thread.currentThread().getId());
			eventCollector.add(event);
		} catch (Exception e) {
			System.out.println("Error while reporting sample from "+classname+"."+method);
			e.printStackTrace();
		}
	}
	
	public static void reportWithThreadInfo(String classname, String method, long startNano, long endNano) {
		try {
			InstrumentationEventWithThreadInfo event = new InstrumentationEventWithThreadInfo(classname, method, convertToTime(startNano), endNano-startNano);
			event.setThreadID(Thread.currentThread().getId());
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			event.setThreadInfo(new ThreadInfo(ThreadDumpHelper.toStackTraceElement(stackTrace,2)));
			eventCollector.add(event);
		} catch (Exception e) {
			System.out.println("Error while reporting sample from "+classname+"."+method);
			e.printStackTrace();
		}
	}

}
