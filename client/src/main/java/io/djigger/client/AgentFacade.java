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

import io.djigger.monitoring.java.agent.JavaAgentMessageType;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

import org.smb.core.Message;
import org.smb.core.MessageListener;
import org.smb.core.MessageRouter;


public class AgentFacade extends Facade implements MessageListener {

    private MessageRouter client;
    
    private boolean providedRouter;

    public AgentFacade(Properties properties, Socket socket) throws IOException {
		super(properties, false);
		providedRouter = true;
		client = new MessageRouter(null, socket);
	}
    
    public AgentFacade(Properties properties) {
		super(properties, true);
		providedRouter = false;
		this.client = null;
	}
    
    @Override
    protected void startSampling() {
    	try {
    		client.sendMessage(JavaAgentMessageType.SUBSCRIBE_THREAD_SAMPLING, getSamplingInterval());
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }

    protected void stopSampling() {
        try {
            client.sendMessage(JavaAgentMessageType.UNSUBSCRIBE_THREAD_SAMPLING, null);
        } catch (IOException e) {
            e.printStackTrace();
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
				listener.instrumentationSamplesReceived((List<InstrumentationSample>) msg.getContent());
			}
		}
	}

	@Override
	protected void close_() {
		if(client!=null) {
			client.disconnect();			
		}
	}

	@Override
	protected void addInstrumentation_(InstrumentSubscription subscription) {
		try {
            client.sendMessage(JavaAgentMessageType.INSTRUMENT, subscription);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	@Override
	protected void removeInstrumentation_(InstrumentSubscription subscription) {
		try {
            client.sendMessage(JavaAgentMessageType.DEINSTRUMENT, subscription);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	@Override
	public void connect_() throws Exception {		
		if(!providedRouter) {
			String host = properties.getProperty("host");
			String port = properties.getProperty("port");
			this.client = new MessageRouter(host,Integer.parseInt(port));
		}
		
		client.start();
		client.registerPermanentListener(JavaAgentMessageType.THREAD_SAMPLE, this);
		client.registerPermanentListener(JavaAgentMessageType.INSTRUMENT_SAMPLE, this);
	}

	@Override
	public boolean isConnected() {
		return client!=null && client.isAlive();
	}
}