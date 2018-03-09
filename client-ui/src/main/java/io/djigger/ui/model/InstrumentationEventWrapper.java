package io.djigger.ui.model;

import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;

public class InstrumentationEventWrapper {

    private InstrumentationEvent event;

    private RealNodePathWrapper eventPath;

    public InstrumentationEventWrapper(InstrumentationEvent event, RealNodePathWrapper eventPath) {
        super();
        this.event = event;
        this.eventPath = eventPath;
    }

    public InstrumentationEvent getEvent() {
        return event;
    }

    public RealNodePathWrapper getEventPath() {
        return eventPath;
    }

}
