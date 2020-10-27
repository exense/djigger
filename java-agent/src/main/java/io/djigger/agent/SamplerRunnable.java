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
import io.djigger.monitoring.java.instrumentation.Transaction;
import io.djigger.monitoring.java.mbeans.MBeanCollector;
import io.djigger.monitoring.java.mbeans.MBeanCollector.ValueListener;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SamplerRunnable implements Runnable {

    private final EventQueue<io.djigger.monitoring.java.model.ThreadInfo> threadInfoQueue;

    private final EventQueue<io.djigger.monitoring.java.model.Metric<?>> metricsQueue;

    private ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

    private MBeanCollector collector;

    public SamplerRunnable(EventQueue<io.djigger.monitoring.java.model.ThreadInfo> threadInfoQueue, EventQueue<io.djigger.monitoring.java.model.Metric<?>> metricsQueue, MBeanCollector collector) {
        super();
        this.threadInfoQueue = threadInfoQueue;
        this.metricsQueue = metricsQueue;

        this.collector = collector;

    }

    @Override
    public void run() {
        ThreadInfo[] infos = mxBean.dumpAllThreads(false, false);
        long timestamp = System.currentTimeMillis();
        for (ThreadInfo threadInfo : infos) {
            io.djigger.monitoring.java.model.ThreadInfo event = ThreadDumpHelper.toThreadInfo(timestamp, threadInfo);
            Transaction currentTransaction = InstrumentationEventCollector.getCurrentTransaction(threadInfo.getThreadId());
            String currentTrID = currentTransaction != null ? currentTransaction.getId().toString() : null;
            event.setTransactionID(currentTrID);
            threadInfoQueue.add(event);
        }

        final List<Metric<?>> metrics = new ArrayList<Metric<?>>();
        collector.collect(new ValueListener() {

            @Override
            public void valueReceived(Metric<?> metric) {
                metrics.add(metric);
            }
        });

        metricsQueue.add(metrics);
    }
}
