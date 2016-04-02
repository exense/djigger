/*******************************************************************************
 * (C) Copyright  2016 Jérôme Comte and others.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *    - Jérôme Comte
 *******************************************************************************/
package io.djigger.client;

import io.djigger.model.Capture;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Facade {
	
	private static final Logger logger = LoggerFactory.getLogger(Facade.class);
	
	private boolean connected;
	
	protected final Properties properties;
	
    protected final List<FacadeListener> listeners = new ArrayList<FacadeListener>();

    private static final int DEFAULT_RATE = 1000;
    
    private int samplingRate;
    
    private boolean samplingState;
    
    private Capture currentCapture;
    
    private final Set<InstrumentSubscription> subscriptions = new HashSet<InstrumentSubscription>();
    
    private Timer timer;
            
    public Facade(final Properties properties, boolean autoReconnect) {
		super();
		this.properties = properties;
		
		this.connected = false;
		this.samplingRate = DEFAULT_RATE;
		
		if(autoReconnect) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						if(!isConnected()) {
								connect();
								restoreSession();
						}
					} catch (Exception e) {
						logger.debug("Unable to reconnect facade " + properties.toString(), e);
					}
				}
				
			}, 10000, 5000);
		}
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
		timer.cancel();
	}
	
	protected abstract void destroy_();
    
	public void addListener(FacadeListener listener) {
		listeners.add(listener);
	}

    public synchronized void addInstrumentation(InstrumentSubscription subscription) {
        subscriptions.add(subscription);
        addInstrumentation_(subscription);
    }
    
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
		if(samplingState) {
			setSampling(false);
			setSampling(true);
		}
	}
	
	public int getSamplingInterval() {
		return samplingRate;
	}
		
	public synchronized void setSampling(boolean state) {
		this.samplingState = state;
		if(state) {
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
		
    private void fireConnectionEvent() {
        for(FacadeListener listener:listeners) {
            try {
            	if(isConnected()) {
            		listener.connectionEstablished();
            	} else {
            		listener.connectionClosed();
            	}
            		
            } catch (Exception e) {
            	logger.error("Error while calling FacadeListener "+listener.toString(), e);
            }
        }
    }
	
    private void fireCaptureStopped() {
        if(currentCapture!=null) {
            currentCapture.setEnd(System.currentTimeMillis());
            currentCapture = null;
        }
    }

    private void fireCaptureStarted() {
    	currentCapture = new Capture(samplingRate);
    }
    

	protected void restoreSession() {
		if(isSampling()) {
			setSampling(true);
		}
		for(InstrumentSubscription s:subscriptions) {
			addInstrumentation_(s);
		}
	}
}