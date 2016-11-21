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

import io.djigger.monitoring.java.agent.JavaAgentMessageType;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smb.core.Message;
import org.smb.core.MessageListener;
import org.smb.core.MessageRouter;


public class AgentFacade extends Facade implements MessageListener {
	
	private static final Logger logger = LoggerFactory.getLogger(AgentFacade.class);

    protected MessageRouter client;
    
    public AgentFacade(Properties properties, boolean autoReconnect) {
    	super(properties, autoReconnect);
    	this.client = null;
    }
        
    public AgentFacade(Properties properties) {
		this(properties, true);
	}
    
    @Override
    protected void startSampling() {
    	if(client!=null) {
	    	try {
	    		client.sendMessage(JavaAgentMessageType.SUBSCRIBE_THREAD_SAMPLING, getSamplingInterval());
	    	} catch (IOException e) {
	    		logger.error("Error while sending message to agent:",e);
	    	}
    	}	
    }

    protected void stopSampling() {
        try {
            client.sendMessage(JavaAgentMessageType.UNSUBSCRIBE_THREAD_SAMPLING, null);
        } catch (IOException e) {
    		logger.error("Error while sending message to agent:",e);
        }
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(Message msg) {
		if(JavaAgentMessageType.THREAD_SAMPLE.equals(msg.getType())) {
			for(FacadeListener listener:listeners) {
				listener.threadInfosReceived((List<ThreadInfo>) msg.getContent());
			}
		} else if (JavaAgentMessageType.INSTRUMENT_SAMPLE.equals(msg.getType())) {
			for(FacadeListener listener:listeners) {
				listener.instrumentationSamplesReceived((List<InstrumentationEvent>) msg.getContent());
			}
		}
	}

	@Override
	protected void destroy_() {
		if(client!=null) {
			client.disconnect();			
		}
	}

	@Override
	protected void addInstrumentation_(InstrumentSubscription subscription) {
		if(client!=null) {
			try {
	            client.sendMessage(JavaAgentMessageType.INSTRUMENT, subscription);
	        } catch (IOException e) {
	    		logger.error("Error while sending message to agent:",e);
	        }
		}
	}

	@Override
	protected void removeInstrumentation_(InstrumentSubscription subscription) {
		try {
            client.sendMessage(JavaAgentMessageType.DEINSTRUMENT, subscription);
        } catch (IOException e) {
    		logger.error("Error while sending message to agent:",e);
        }
	}

	@Override
	public void connect_() throws Exception {		
		String host = properties.getProperty("host");
		String port = properties.getProperty("port");
		this.client = new MessageRouter(host,Integer.parseInt(port));
		
		startClient();
	}

	protected void startClient() {
		client.start();
		client.registerPermanentListener(JavaAgentMessageType.THREAD_SAMPLE, this);
		client.registerPermanentListener(JavaAgentMessageType.INSTRUMENT_SAMPLE, this);
	}

	@Override
	public boolean isConnected() {
		return client!=null && client.isAlive();
	}

	@Override
	public boolean hasInstrumentationSupport() {
		return true;
	}
}