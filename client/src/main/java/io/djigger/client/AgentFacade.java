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

import io.denkbar.smb.core.Message;
import io.denkbar.smb.core.MessageListener;
import io.denkbar.smb.core.MessageRouter;
import io.djigger.agent.InstrumentationError;
import io.djigger.monitoring.java.agent.JavaAgentMessageType;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.model.ThreadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;


public class AgentFacade extends Facade implements MessageListener {

    private static final int DEFAULT_CALLTIMEOUT = 10000;

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
        if (client != null) {
            try {
                client.sendMessage(JavaAgentMessageType.SUBSCRIBE_THREAD_SAMPLING, getSamplingInterval());
            } catch (IOException e) {
                logger.error("Error while sending message to agent:", e);
            }
            subscribeToMetricCollection();
        }
    }

    protected void subscribeToMetricCollection() {
        if (metricCollectionConfiguration != null) {
            try {
                client.sendMessage(JavaAgentMessageType.SUBSCRIBE_METRIC_COLLECTION, metricCollectionConfiguration.getmBeans());
            } catch (IOException e) {
                logger.error("Error while sending message to agent:", e);
            }
        }
    }

    protected void stopSampling() {
        try {
            client.sendMessage(JavaAgentMessageType.UNSUBSCRIBE_THREAD_SAMPLING, null);
        } catch (IOException e) {
            logger.error("Error while sending message to agent:", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(Message msg) {
        if (JavaAgentMessageType.THREAD_SAMPLE.equals(msg.getType())) {
            for (FacadeListener listener : listeners) {
                listener.threadInfosReceived((List<ThreadInfo>) msg.getContent());
            }
        } else if (JavaAgentMessageType.INSTRUMENT_SAMPLE.equals(msg.getType())) {
            for (FacadeListener listener : listeners) {
                listener.instrumentationSamplesReceived((List<InstrumentationEvent>) msg.getContent());
            }
        } else if (JavaAgentMessageType.METRICS.equals(msg.getType())) {
            for (FacadeListener listener : listeners) {
                listener.metricsReceived((List<Metric<?>>) msg.getContent());
            }
        } else if (JavaAgentMessageType.INSTRUMENTATION_ERROR.equals(msg.getType())) {
        	InstrumentationError error = (InstrumentationError) msg.getContent();
            for (FacadeListener listener : listeners) {
                listener.instrumentationErrorReceived(error);
            }
        }
    }

    @Override
    protected void destroy_() {
        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    protected void addInstrumentation_(InstrumentSubscription subscription) {
        if (client != null) {
            try {
                client.sendMessage(JavaAgentMessageType.INSTRUMENT, subscription);
            } catch (IOException e) {
                logger.error("Error while sending message to agent:", e);
            }
        }
    }

    @Override
    protected void removeInstrumentation_(InstrumentSubscription subscription) {
        try {
            client.sendMessage(JavaAgentMessageType.DEINSTRUMENT, subscription);
        } catch (IOException e) {
            logger.error("Error while sending message to agent:", e);
        }
    }

    @Override
    public void connect_() throws Exception {
        String host = properties.getProperty(Parameters.HOST);
        String port = properties.getProperty(Parameters.PORT);
        this.client = new MessageRouter(host, Integer.parseInt(port));

        startClient();
    }

    protected void startClient() {
        client.start();
        client.registerPermanentListener(JavaAgentMessageType.THREAD_SAMPLE, this);
        client.registerPermanentListener(JavaAgentMessageType.INSTRUMENT_SAMPLE, this);
        client.registerPermanentListener(JavaAgentMessageType.METRICS, this);
        client.registerPermanentListener(JavaAgentMessageType.INSTRUMENTATION_ERROR, this);
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isAlive();
    }

    @Override
    public boolean hasInstrumentationSupport() {
        return true;
    }

	@Override
	public byte[] getClassBytecode(String classname) throws Exception {
		return (byte[]) client.call(new Message(JavaAgentMessageType.GET_CLASS_BYTECODE, classname), DEFAULT_CALLTIMEOUT);
	}
}
