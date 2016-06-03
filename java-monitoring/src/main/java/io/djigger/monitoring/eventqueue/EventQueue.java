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
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventQueue<T>  {
	
	private static final Logger logger = Logger.getLogger(EventQueue.class.getName());
	
	// TODO: the following performs better but is not available in java versions <= 6. Implement a kind of switch
	//private final ConcurrentLinkedDeque<T> bufferIn = new ConcurrentLinkedDeque<T>();
	
	private final LinkedBlockingDeque<T> bufferIn = new LinkedBlockingDeque<T>();
		
	private volatile boolean skip=false;
	
	private volatile boolean skipAll=false;
	
	private volatile byte skipLevel = 1;
	
	private static final int SKIP_ALL_TRESHHOLD = 5;
	
	private final EventQueueConsumer<T> consumer;
		
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	private final ThreadPoolExecutor consumerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	
	private final EventSkipLogic<T> skipLogic;
	
	public EventQueue(long period, TimeUnit timeUnit, EventQueueConsumer<T> consumer, EventSkipLogic<T> skipLogic) {
		super();
		
		this.consumer = consumer;
		this.skipLogic = skipLogic;
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
				if(skipLevel<SKIP_ALL_TRESHHOLD) {
					skipLevel++;					
				} else {
					skipAll = true;
					logger.log(Level.WARNING, "Skipping all events.");
				}
			} else {				
				skip = true;
				skipLevel = 1;
			}
		} else {
			if(skip) {
				// decrease the skip level
				if(skipLevel==1) {
					skip = false;	
				} else {
					if(skipAll) {
						skipAll = false;
						logger.log(Level.INFO, "Disabling skipping of all events.");
					} else {
						skipLevel--;
					}
				}
			} else {
				// this is the normal case. Nothing special to do.
			}
		}
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Skip modulo: "+skipLevel+", SkipAll: "+skipAll);
		}		
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
		
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Read buffer with " + bufferOut.size() + " in "+(System.nanoTime()-t1)/1000000);
		}
		return bufferOut;
	}

	public void add(T event) {
		if(isAccepted(event)) {
			bufferIn.offer(event);
		}
	}
	
	public void add(Collection<T> event) {
		for (T t : event) {
			add(t);
		}
	}
	
	private boolean isAccepted(T object) {
		return !skipAll && (!skip || !skipLogic.isSkipped(object, skipLevel));
	}
	
	public interface EventQueueConsumer<T> {
		public void processBuffer(LinkedList<T> collector);
	}

	public void shutdown() {
		skipAll = true;
		scheduler.shutdown();
		consumerPool.shutdown();
	}

	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return consumerPool.awaitTermination(timeout, unit) && 
				scheduler.awaitTermination(timeout, unit);
	}

	public boolean isSkipAll() {
		return skipAll;
	}
}
