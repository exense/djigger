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
package io.djigger.store.filter;

import io.djigger.model.TaggedMetric;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;

import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ql.Filter;

public class StoreFilter {

    private Filter<ThreadInfo> threadInfoFilter;

    private Filter<InstrumentationEvent> instrumentationEventsFilter;

    private Filter<TaggedMetric> metricFilter;

    public StoreFilter(Filter<ThreadInfo> threadInfoFilter, Filter<InstrumentationEvent> instrumentationEventsFilter, Filter<TaggedMetric> metricFilter) {
        super();
        this.threadInfoFilter = threadInfoFilter;
        this.instrumentationEventsFilter = instrumentationEventsFilter;
        this.metricFilter = metricFilter;
    }

    public Filter<TaggedMetric> getMetricFilter() {
        return metricFilter;
    }

    public void setMetricFilter(Filter<TaggedMetric> metricFilter) {
        this.metricFilter = metricFilter;
    }

    public Filter<ThreadInfo> getThreadInfoFilter() {
        return threadInfoFilter;
    }

    public void setThreadInfoFilter(Filter<ThreadInfo> threadInfoFilter) {
        this.threadInfoFilter = threadInfoFilter;
    }

    public Filter<InstrumentationEvent> getInstrumentationEventsFilter() {
        return instrumentationEventsFilter;
    }

    public void setInstrumentationEventsFilter(Filter<InstrumentationEvent> instrumentationEventsFilter) {
        this.instrumentationEventsFilter = instrumentationEventsFilter;
    }
}
