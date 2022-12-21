package io.djigger.monitoring.eventqueue;

import io.djigger.monitoring.eventqueue.EventQueue.EventQueueConsumer;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EventQueueTest {

    ExecutorService s;

    EventQueue<Integer> c;

    AtomicInteger countIn = new AtomicInteger();
    AtomicInteger countOut = new AtomicInteger();

    volatile int throughputInPerMs;

    volatile int throughputOutPerMs;

    @Test
    public void test1() throws InterruptedException {
        throughputInPerMs = 500;
        throughputOutPerMs = 100;

        run(5);

        Thread.sleep(800);
        Assert.assertTrue(c.isSkipAll());

        throughputOutPerMs = 1000;
        Thread.sleep(800);
        Assert.assertFalse(c.isSkipAll());

        awaitTermination();
        Assert.assertTrue(c.isSkipAll());
        Assert.assertTrue(countOut.intValue() < countIn.intValue());
    }

    @Test
    public void test2() throws InterruptedException {
        throughputInPerMs = 100;
        throughputOutPerMs = 200;

        run(5);

        Thread.sleep(2000);
        Assert.assertFalse(c.isSkipAll());

        awaitTermination();
        Assert.assertTrue(c.isSkipAll());
        Assert.assertEquals(countIn.intValue(), countOut.intValue());
    }

    private void run(int nThreads) throws InterruptedException {
        c = initCollector();
        s = initProducers(c, nThreads);
    }

    private void awaitTermination() throws InterruptedException {
        s.shutdown();
        s.awaitTermination(1, TimeUnit.MINUTES);

        // wait 2 cycles to ensure that the consumers
        Thread.sleep(200);

        c.shutdown();
        c.awaitTermination(1, TimeUnit.MINUTES);
    }

    private ExecutorService initProducers(final EventQueue<Integer> c, final int nThreads) {
        ScheduledExecutorService s = Executors.newScheduledThreadPool(nThreads);

        countIn = new AtomicInteger();
        for (int i = 0; i < nThreads; i++) {
            s.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    for (long j = 0; j < throughputInPerMs / nThreads; j++) {
                        c.add(countIn.incrementAndGet());
                    }
                }
            }, 0, 1, TimeUnit.MILLISECONDS);
        }
        return s;
    }

    private EventQueue<Integer> initCollector() {
        countOut = new AtomicInteger();
        final EventQueue<Integer> c = new EventQueue<Integer>(100, TimeUnit.MILLISECONDS, new EventQueueConsumer<Integer>() {
            @Override
            public void processBuffer(LinkedList<Integer> buffer) {
                long sleep = buffer.size() / throughputOutPerMs;
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                countOut.addAndGet(buffer.size());
            }
        }, new ModuloEventSkipLogic<Integer>() {
            @Override
            protected long getSkipAttribute(Integer object) {
                return object;
            }
        });
        return c;
    }
}
