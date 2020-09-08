package io.djigger.model;

import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;
import io.djigger.monitoring.java.model.Metric;

import java.util.Map;

public class TaggedMetric extends AbstractOrganizableObject {

    private Map<String, String> tags;

    private Metric<?> metric;

    public TaggedMetric() {
        super();
    }

    public TaggedMetric(Map<String, String> tags, Metric<?> metric) {
        super();
        this.tags = tags;
        this.metric = metric;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Metric<?> getMetric() {
        return metric;
    }
}
