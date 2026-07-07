package io.djigger.it.support;

import io.djigger.agent.InstrumentationError;
import io.djigger.client.FacadeListener;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A {@link FacadeListener} that records everything it receives, for assertions in the integration tests.
 */
public class CollectingFacadeListener implements FacadeListener {

    public final List<ThreadInfo> threadInfos = new CopyOnWriteArrayList<>();
    public final List<InstrumentationEvent> instrumentationEvents = new CopyOnWriteArrayList<>();
    public final List<Metric<?>> metrics = new CopyOnWriteArrayList<>();
    public volatile boolean connectionEstablished;
    public volatile boolean connectionClosed;

    @Override
    public void threadInfosReceived(List<ThreadInfo> threaddumps) {
        threadInfos.addAll(threaddumps);
    }

    @Override
    public void instrumentationSamplesReceived(List<InstrumentationEvent> samples) {
        instrumentationEvents.addAll(samples);
    }

    @Override
    public void instrumentationErrorReceived(InstrumentationError error) {
    }

    @Override
    public void metricsReceived(List<Metric<?>> metricList) {
        metrics.addAll(metricList);
    }

    @Override
    public void connectionEstablished() {
        connectionEstablished = true;
    }

    @Override
    public void connectionClosed() {
        connectionClosed = true;
    }
}
