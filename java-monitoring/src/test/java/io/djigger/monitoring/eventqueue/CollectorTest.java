package io.djigger.monitoring.eventqueue;

import io.djigger.monitoring.eventqueue.EventQueue.EventQueueConsumer;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class CollectorTest {

	private class EventImpl implements Event {

		@Override
		public long getTimestamp() {
			return System.nanoTime();
		}
		
	}
	
	@Test 
	public void test1() throws InterruptedException {
		int nThreads = 5;
		final long nIterations = 100000;

		final AtomicInteger count = new AtomicInteger();

		final EventQueue<EventImpl> c = run(nThreads, nIterations, count, 150);
		
		Assert.assertTrue(c.isSkipAll());
		Assert.assertTrue(count.intValue()<nThreads*nIterations);
	}
	
	@Test 
	public void test2() throws InterruptedException {
		int nThreads = 5;
		final long nIterations = 100000;

		final AtomicInteger count = new AtomicInteger();

		final EventQueue<EventImpl> c = run(nThreads, nIterations, count, 0);
		
		Assert.assertFalse(c.isSkipAll());
		Assert.assertEquals(nThreads*nIterations, count.intValue());
	}
	
	@Test 
	public void test3() throws InterruptedException {
		int nThreads = 5;
		final long nIterations = 100000;

		final AtomicInteger count = new AtomicInteger();

		final EventQueue<EventImpl> c = run(nThreads, nIterations, count, 10);
		
		Assert.assertFalse(c.isSkipAll());
		Assert.assertEquals(nThreads*nIterations, count.intValue());
	}

	private EventQueue<EventImpl> run(int nThreads, final long nIterations,
			final AtomicInteger count, long sleep) throws InterruptedException {
		final EventQueue<EventImpl> c = initCollector(count, sleep);
			
		ExecutorService s = initProducers(c, nThreads, nIterations);
				
		awaitTermination(c, s);
		return c;
	}

	private void awaitTermination(final EventQueue<EventImpl> c, ExecutorService s)
			throws InterruptedException {
		s.shutdown();
		s.awaitTermination(1, TimeUnit.MINUTES);
		
		
		Thread.sleep(200);
		c.shutdown();
		c.awaitTermination(1, TimeUnit.MINUTES);
	}

	private ExecutorService initProducers(final EventQueue<EventImpl> c, int nThreads,
			final long nIterations) {
		ExecutorService s = Executors.newFixedThreadPool(nThreads);
		for(int i=0;i<nThreads;i++) {
			s.submit(new Runnable() {
				@Override
				public void run() {
					for(long j=0;j<nIterations;j++) {
						c.add(new EventImpl());
					}
				}
			});
		}
		return s;
	}

	private EventQueue<EventImpl> initCollector(final AtomicInteger count, final long sleep) {
		final EventQueue<EventImpl> c = new EventQueue<EventImpl>(100, TimeUnit.MILLISECONDS, new EventQueueConsumer<EventImpl>() {
			@Override
			public void processBuffer(LinkedList<EventImpl> buffer) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				count.addAndGet(buffer.size());
			}
		});
		return c;
	}
}
