package io.djigger.monitoring.java.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Metric<T> implements Serializable {

    private static final long serialVersionUID = 5997085581445652199L;

    private long time;

    private String name;

    private T value;

    private Map<String,String> attributes;

    public Metric(String name, T value) {
        super();
        this.name = name;
        this.value = value;
    }

    public Metric(long time, String name, T value) {
        super();
        this.time = time;
        this.name = name;
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
