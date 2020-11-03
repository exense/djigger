package io.djigger.model;

import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;
import io.djigger.monitoring.java.model.Metric;

import java.io.Serializable;
import java.util.Map;

public class TaggedMetric extends AbstractOrganizableObject implements Serializable  {

    private static final long serialVersionUID = 8672664655551132300L;

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

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void setMetric(Metric<?> metric) {
        this.metric = metric;
    }
}
