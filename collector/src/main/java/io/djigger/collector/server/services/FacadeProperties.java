package io.djigger.collector.server.services;

import io.djigger.client.Facade;
import io.djigger.collector.server.ClientConnection;
import io.djigger.model.Connection;

import java.util.*;

public class FacadeProperties {

    String connectionId;

    String facadeClass;

    Map<String, String> attributes;

    Map<String, String> properties;

    boolean connected;

    boolean connectionEnabled;

    int samplingRate;

    boolean samplingActive;

    public FacadeProperties(ClientConnection clientConnection) {
        super();
        Facade facade = clientConnection.getFacade();
        this.connectionId = facade.getConnectionId();
        this.facadeClass = facade.getClass().getSimpleName();
        this.attributes = sorted(clientConnection.getAttributes());
        this.properties = sorted(facade.getProperties());
        this.connected = facade.isConnected();
        this.connectionEnabled = facade.isConnectionEnabled();
        this.samplingRate = facade.getSamplingInterval();
        this.samplingActive = facade.isSampling();
    }

    public String getConnectionId() { return connectionId; }

    public String getFacadeClass() {
        return facadeClass;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public boolean isSamplingActive() {
        return samplingActive;
    }

    public boolean isConnectionEnabled() {
        return connectionEnabled;
    }

    public void setConnectionEnabled(boolean connectionEnabled) {
        this.connectionEnabled = connectionEnabled;
    }

    private SortedMap<String, String> sorted(Map<String, String> map) {
        // force a simple (alphabetical) sort
        TreeMap<String, String> sorted = new TreeMap<>((Comparator<String>) null);
        sorted.putAll(map);
        return sorted;
    }

    private SortedMap<String, String> sorted(Properties props) {
        // sort according to the logic defined in Facade.Parameters
        SortedMap<String, String> sorted = new TreeMap<>(Connection.Parameters.SORT_COMPARATOR);
        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            sorted.put(key, value);
        }
        return sorted;
    }

}
