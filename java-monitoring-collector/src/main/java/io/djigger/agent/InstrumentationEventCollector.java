/*******************************************************************************
 * (C) Copyright 2016 Jérôme Comte and Dorian Cransac
 *  
 *  This file is part of djigger
 *  
 *  djigger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  djigger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with djigger.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package io.djigger.agent;

import io.djigger.monitoring.eventqueue.EventQueue;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventWithThreadInfo;
import io.djigger.monitoring.java.instrumentation.Transaction;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

public class InstrumentationEventCollector {

	private static EventQueue<InstrumentationEvent> eventCollector;
	
	private static long tRef = System.currentTimeMillis();
	private static long tRefNano = System.nanoTime();
	
	private static ThreadLocal<Transaction> transactions = new ThreadLocal<>(); 
	
	private static long convertToTime(long tNano) {
		return (tNano-tRefNano)/1000000+tRef;
	}

	public static void setEventCollector(EventQueue<InstrumentationEvent> eventCollector) {
		InstrumentationEventCollector.eventCollector = eventCollector;
	}

	public static void enterMethod(String classname, String method, boolean addThreadInfo) {
		InstrumentationEvent event;
		
		if(addThreadInfo) {
			event = new InstrumentationEventWithThreadInfo(classname, method);
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			((InstrumentationEventWithThreadInfo) event).setThreadInfo(new ThreadInfo(ThreadDumpHelper.toStackTraceElement(stackTrace,2)));
		} else {
			event = new InstrumentationEvent(classname, method);
		}
		
		Transaction transaction = transactions.get();
		if(transaction == null) {
			transaction = new Transaction();
			transactions.set(transaction);		
		} else {
			InstrumentationEvent currentEvent = transaction.peekEvent();
			
			long localParentId = currentEvent.getLocalID();
			event.setLocalParentID(localParentId);
		}
		
		long localId = transaction.getNextCallId();
		event.setLocalID(localId);
		
		event.setTransactionID(transaction.getId());
		
		transaction.pushEvent(event);

		long startNano = System.nanoTime();
		event.setStartNano(System.nanoTime());
		event.setStart(convertToTime(startNano));
	}
	
	public static void leaveMethod() {
		long endNano = System.nanoTime();

		Transaction transaction = transactions.get();
		InstrumentationEvent event = transaction.popEvent();
		event.setDuration(endNano-event.getStartNano());
		
		eventCollector.add(event);

		if(event.getLocalID()==0) {
			transactions.remove();
		}
	}


}
