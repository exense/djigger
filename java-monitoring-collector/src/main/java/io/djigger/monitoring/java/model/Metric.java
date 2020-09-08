package io.djigger.monitoring.java.model;

import java.io.Serializable;
import java.util.Date;

public class Metric<T> implements Serializable {

    private static final long serialVersionUID = 5997085581445652199L;

    private Date time;

    private String name;

    private T value;

    public Metric() {
        super();
    }

    public Metric(String name, T value) {
        super();
        this.name = name;
        this.value = value;
    }

    public Metric(Date time, String name, T value) {
        super();
        this.time = time;
        this.name = name;
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
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
}
