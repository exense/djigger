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

import io.djigger.client.mbeans.MetricCollectionConfiguration;
import io.djigger.model.Capture;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Facade {

    public static class Parameters {
        // used by JStackLogTailFacade
        public static final String FILE = "file";
        public static final String START_AT_FILE_BEGIN = "startAtFileBegin";

        // used by ProcessAttachFacade
        public static final String PROCESS_ID = "processID";
        public static final String PROCESS_NAME_PATTERN = "processNamePattern";
        public static final String CONNECTION_TIMEOUT = "connectionTimeoutMs";

        // used by AgentFacade and JMXClientFacade
        public static final String HOST = "host";
        public static final String PORT = "port";

        // used by JMXClientFacade
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";

        /*
         Defines the order in which the parameters should be shown when listed.
         This is relevant for the collector status page, to guarantee a consistent and
         logical order, e.g: host, port, username, password. This, in turn, also makes
         the sort capabilities on the web page more useful.

         The comparator also accepts values not contained in the list of known parameter
         names. They will be sorted in alphabetical order, after the known ones.
        */
        public static final Comparator<String> SORT_COMPARATOR = new Comparator<String>() {
            private final List<String> knownParamsOrder = Arrays.asList(
                FILE, START_AT_FILE_BEGIN,
                PROCESS_ID, PROCESS_NAME_PATTERN, CONNECTION_TIMEOUT,
                HOST, PORT, USERNAME, PASSWORD
            );

            @Override
            public int compare(String s1, String s2) {
                int i1 = knownParamsOrder.indexOf(s1);
                int i2 = knownParamsOrder.indexOf(s2);

                if (i1 >= 0 && i2 >= 0) {
                    // both are known: compare by index in knownParamsOrder
                    return Integer.compare(i1, i2);
                } else if (i1 < 0 && i2 < 0) {
                    // both are unknown: compare by name
                    return s1.compareToIgnoreCase(s2);
                } else {
                    // exactly one is known (>=0), and one is unknown (<0).
                    // put the known one first, which is equivalent to a reverse integer sort
                    return -Integer.compare(i1, i2);
                }
            }
        };

    }

    private static final Logger logger = LoggerFactory.getLogger(Facade.class);

    /**
     * A unique ID of the connection
     */
    private final String connectionId;

    private boolean connected;

    protected final Properties properties;

    protected final List<FacadeListener> listeners = new ArrayList<FacadeListener>();

    private static final int DEFAULT_RATE = 1000;

    private int samplingRate;

    private boolean samplingState;

    private Capture currentCapture;

    private final Set<InstrumentSubscription> subscriptions = new HashSet<InstrumentSubscription>();

    private Timer timer;

    protected MetricCollectionConfiguration metricCollectionConfiguration;

    public Facade(final Properties properties, boolean autoReconnect) {
        super();
        this.properties = properties;

        this.connectionId = UUID.randomUUID().toString();

        this.connected = false;
        this.samplingRate = DEFAULT_RATE;

        if (autoReconnect) {
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
    }

    public String getConnectionId() {
		return connectionId;
	}

	public Properties getProperties() {
        return properties;
    }

    public void connect() throws Exception {
        connect_();
        connected = true;
        fireConnectionEvent();
    }

    protected abstract void connect_() throws Exception;

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
