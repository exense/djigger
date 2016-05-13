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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.types.ObjectId;

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
	
	private static Map<Long, Transaction> transactionMap = new ConcurrentHashMap<>();
	
	private static long convertToTime(long tNano) {
		return (tNano-tRefNano)/1000000+tRef;
	}

	public static void setEventCollector(EventQueue<InstrumentationEvent> eventCollector) {
		InstrumentationEventCollector.eventCollector = eventCollector;
	}
	
	public static String getCurrentTracer() {
		Transaction tr = transactions.get();
		return tr!=null?tr.getId().toString()+tr.peekEvent().getId().toString():null;
	}

	public static void applyTracer(String tracer) {
		UUID trid = UUID.fromString(tracer.substring(0, 36));
		ObjectId parentId = new ObjectId(tracer.substring(36));
		Transaction tr = transactions.get();
		if(tr==null) {
			tr = new Transaction(trid);
			setCurrentTransaction(tr);
		} else {
			tr.setId(trid);
		}
		tr.setParentId(parentId);
	}
	
	public static void leaveTransaction() {
		transactions.remove();
		transactionMap.remove(Thread.currentThread().getId());
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
		
		event.setId(new ObjectId());
		
		Transaction transaction = transactions.get();
		if(transaction == null) {
			transaction = new Transaction();
			setCurrentTransaction(transaction);
		} else {
			if(transaction.getParentId()!=null) {
				event.setParentID(transaction.getParentId());
				transaction.setParentId(null);
			} else {
				InstrumentationEvent currentEvent = transaction.peekEvent();
				event.setParentID(currentEvent.getId());				
			}
		}
		
		event.setThreadID(Thread.currentThread().getId());
		
		transaction.pushEvent(event);

		long startNano = System.nanoTime();
		event.setStartNano(System.nanoTime());
		event.setStart(convertToTime(startNano));
	}

	private static void setCurrentTransaction(Transaction transaction) {
		transactions.set(transaction);	
		transactionMap.put(Thread.currentThread().getId(), transaction);
	}
	
	public static void leaveMethod() {
		long endNano = System.nanoTime();

		Transaction transaction = transactions.get();
		InstrumentationEvent event = transaction.popEvent();
		event.setDuration(endNano-event.getStartNano());
		
		event.setTransactionID(transaction.getId());

		eventCollector.add(event);

		if(transaction.isStackEmpty()) {
			leaveTransaction();
		}
	}
	
	public static Transaction getCurrentTransaction(long threadID) {
		return transactionMap.get(threadID);
	}


}
