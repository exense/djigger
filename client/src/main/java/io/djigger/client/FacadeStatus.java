package io.djigger.client;

import java.util.Map;

public class FacadeStatus {

    String connectionId;

    String facadeClass;

    Map<String, String> attributes;

    Map<String, String> properties;

    boolean connected;

    int samplingRate;

    boolean samplingActive;

    public FacadeStatus(String connectionId, String facadeClass, Map<String, String> attributes, Map<String, String> properties,
                        boolean connected, int samplingRate, boolean samplingActive) {
        super();
        this.connectionId = connectionId;
        this.facadeClass = facadeClass;
        this.attributes = attributes;
        this.properties = properties;
        this.connected = connected;
        this.samplingRate = samplingRate;
        this.samplingActive = samplingActive;
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

}
