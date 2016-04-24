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
package io.djigger.monitoring.eventqueue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class EventQueue<T extends Event>  {
	
	private final ConcurrentLinkedDeque<T> bufferIn = new ConcurrentLinkedDeque<T>();
		
	private volatile boolean skip=false;
	
	private volatile boolean skipAll=false;
	
	private volatile int skipModulo = 1;
	
	private volatile int skipIncFactor = 5;
	
	private static final int SKIP_MODULO_LIMIT = 1000;
	
	private final EventQueueConsumer<T> consumer;
		
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	private final ThreadPoolExecutor consumerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	
	public EventQueue(long period, TimeUnit timeUnit, EventQueueConsumer<T> consumer) {
		super();
		
		this.consumer = consumer;
		scheduler.scheduleAtFixedRate(new Task(), 0, period, timeUnit);
	}
	
	private class Task implements Runnable {
		@Override
		public void run() {	
			activateSkipIfNeeded();
			
			LinkedList<T> bufferOut = drainBuffer();
			
			submitToConsumers(bufferOut);
		}
	}
	
	private void activateSkipIfNeeded() {
		if(consumerPool.getActiveCount()>0) {
			if(skip) {
				if(skipModulo<SKIP_MODULO_LIMIT) {
					skipModulo*=skipIncFactor;					
				} else {
					skipAll = true;
				}
				//System.out.println("Increasing skip modulo");
			} else {				
				skip = true;
				skipModulo = 1;
				//System.out.println("Activating skipping");
			}
		} else {
			if(skip) {
				// decrease the skip modulo
				if(skipModulo==1) {
					skip = false;	
					//System.out.println("Deactivating skipping");
				} else {
					if(skipAll) {
						skipAll = false;
					} else {
						skipModulo/=skipIncFactor;
					}
					//System.out.println("Decreasing skip modulo");
				}
			} else {
				// this is the normal case. Nothing special to do.
			}
		}
		System.out.println("Skip modulo: "+skipModulo);
		
	}
	
	private void submitToConsumers(final LinkedList<T> bufferOut) {
		consumerPool.submit(new Runnable() {
			@Override
			public void run() {
				consumer.processBuffer(bufferOut);
			}
		});
	}
	
	private LinkedList<T> drainBuffer() {
		long t1 = System.nanoTime();
		
		final LinkedList<T> bufferOut = new LinkedList<T>();
		
		T last = bufferIn.peekLast();
		T event;
		while((event=bufferIn.poll())!=null) {
			bufferOut.add(event);	
			if(event==last) {
				break;
			}
		}
		
		System.out.println("Read buffer in "+(System.nanoTime()-t1)/1000000);
		return bufferOut;
	}

	public void add(T event) {
		if(isAccepted(event.getTimestamp())) {
			bufferIn.offer(event);
		}
	}
	
	public void add(Collection<T> event) {
		for (T t : event) {
			add(t);
		}
	}
	
	private boolean isAccepted(long nanoTime) {
		return !skipAll && (!skip || nanoTime%skipModulo==0);
	}
	
	public interface EventQueueConsumer<T> {
		public void processBuffer(LinkedList<T> collector);
	}

	public void shutdown() {
		consumerPool.shutdown();
		scheduler.shutdown();
	}

	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return consumerPool.awaitTermination(timeout, unit) && 
				scheduler.awaitTermination(timeout, unit);
	}

	public boolean isSkipAll() {
		return skipAll;
	}
}
