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
package io.djigger.client;

import io.djigger.mbeans.MetricCollectionConfiguration;
import io.djigger.model.Capture;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Facade {

    private static final Logger logger = LoggerFactory.getLogger(Facade.class);

    /**
     * A unique ID of the connection
     */
    private final String connectionId;

    private boolean autoReconnect;

    private boolean connected;

    private boolean connectionEnabled;

    protected final Properties properties;

    protected final List<FacadeListener> listeners = new ArrayList<FacadeListener>();

    private static final int DEFAULT_RATE = 1000;

    private int samplingRate;

    private boolean samplingState;

    private Capture currentCapture;

    private final Set<InstrumentSubscription> subscriptions = new HashSet<InstrumentSubscription>();

    private Timer timer;

    protected MetricCollectionConfiguration metricCollectionConfiguration;

    public Facade(String connectionId, final Properties properties, boolean autoReconnect) {
        super();
        this.properties = properties;

        this.connectionId = (connectionId!=null) ? connectionId : UUID.randomUUID().toString();

        this.connected = false;
        this.samplingRate = DEFAULT_RATE;

        this.autoReconnect = autoReconnect;
    }

    public void initConnection() {
        if (autoReconnect) {
            createReconnectTimer();
        } else {
            try {
                connect();
            } catch (Exception e) {
                logger.error("Initial connection of facade failed.", e);
            }
            restoreSession();
        }
    }

    protected void createReconnectTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!isConnected()) {
                        connect();
                        restoreSession();
                    }
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unable to reconnect facade " + properties.toString(), e);
                    } else {
                        logger.warn("Unable to reconnect facade " + properties.toString());
                    }
                }
            }

        }, 10000, 5000);
    }

    public String getConnectionId() {
		return connectionId;
	}

	public Properties getProperties() {
        return properties;
    }

    public void toggleConnection(boolean newStateOn) throws Exception {
        this.connectionEnabled = newStateOn;
        if (isConnected() && !newStateOn) {
            destroy();
         } else if (!isConnected() && newStateOn) {
            if (autoReconnect) {
                createReconnectTimer();
            } else {
                connect();
            }
            restoreSession();
        }
    }

    public void connect() throws Exception {
        connect_();
        connected = true;
        fireConnectionEvent();
    }

    protected abstract void connect_() throws Exception;

    public abstract String getConnectionPropertiesString();

    public abstract boolean supportMultipleConnection();

    public boolean isConnected() {
        return connected;
    }

    protected void handleConnectionClosed() {
        connected = false;
        fireCaptureStopped();
        fireConnectionEvent();
    }

    public void destroy() {
        destroy_();
        if (timer != null) {
            timer.cancel();
        }
    }

    protected abstract void destroy_();

    public void addListener(FacadeListener listener) {
        listeners.add(listener);
    }

    private static AtomicInteger idSequence = new AtomicInteger();

    public synchronized void addInstrumentation(InstrumentSubscription subscription) {
        if (subscription.getId() == 0) {
            subscription.setId(idSequence.incrementAndGet());
        }
        subscriptions.add(subscription);
        addInstrumentation_(subscription);
    }

    public abstract boolean hasInstrumentationSupport();

    protected abstract void addInstrumentation_(InstrumentSubscription subscription);

    public synchronized void removeInstrumentation(InstrumentSubscription subscription) {
        subscriptions.remove(subscription);
        removeInstrumentation_(subscription);
    }

    protected abstract void removeInstrumentation_(InstrumentSubscription subscription);

    public synchronized Set<InstrumentSubscription> getInstrumentationSubscriptions() {
        return subscriptions;
    }

    public void setSamplingInterval(int rate) {
        samplingRate = rate;
        if (samplingState) {
            setSampling(false);
            setSampling(true);
        }
    }

    public int getSamplingInterval() {
        return samplingRate;
    }

    public synchronized void setSampling(boolean state) {
        this.samplingState = state;
        if (state) {
            startSampling();
            fireCaptureStarted();
        } else {
            stopSampling();
            fireCaptureStopped();
        }
    }

    protected abstract void startSampling();

    protected abstract void stopSampling();

    public boolean isSampling() {
        return samplingState;
    }

    public boolean isConnectionEnabled() {
        return connectionEnabled;
    }

    public void setConnectionEnabled(boolean connectionEnabled) {
        this.connectionEnabled = connectionEnabled;
    }

    public MetricCollectionConfiguration getMetricCollectionConfiguration() {
        return metricCollectionConfiguration;
    }

    public void setMetricCollectionConfiguration(MetricCollectionConfiguration metricCollectionConfiguration) {
        this.metricCollectionConfiguration = metricCollectionConfiguration;
    }

    private void fireConnectionEvent() {
        for (FacadeListener listener : listeners) {
            try {
                if (isConnected()) {
                    listener.connectionEstablished();
                } else {
                    listener.connectionClosed();
                }

            } catch (Exception e) {
                logger.error("Error while calling FacadeListener " + listener.toString(), e);
            }
        }
    }

    private void fireCaptureStopped() {
        if (currentCapture != null) {
            currentCapture.setEnd(System.currentTimeMillis());
            currentCapture = null;
        }
    }

    private void fireCaptureStarted() {
        currentCapture = new Capture(samplingRate);
    }


    protected void restoreSession() {
        if (isSampling()) {
            setSampling(true);
        }
        for (InstrumentSubscription s : subscriptions) {
            addInstrumentation_(s);
        }
    }

    public boolean hasStartStopSupport() {
        return true;
    }
    
    public byte[] getClassBytecode(String classname) throws Exception {
    	throw new RuntimeException("Unsupported operation");
    }
}
