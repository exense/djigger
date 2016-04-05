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
package io.djigger.agent;

import io.djigger.monitoring.java.agent.JavaAgentMessageType;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.sampling.Sampler;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.Socket;

import org.smb.core.Message;
import org.smb.core.MessageListener;
import org.smb.core.MessageRouter;
import org.smb.core.MessageRouterStateListener;


public class AgentSession implements MessageListener, MessageRouterStateListener {

	private final MessageRouter messageRouter;

	private volatile boolean isAlive;
	
	private final Sampler sampler;
	
	private final SamplerRunnable samplerRunnable;
	
	private final InstrumentationService instrumentationService;
	
	private final PostService postService;

	public AgentSession(Socket socket, Instrumentation instrumentation) throws IOException {
		super();
		
		this.messageRouter = new MessageRouter(this,socket);;
		messageRouter.registerPermanentListenerForAllMessages(this);
		this.isAlive = true;

		samplerRunnable = new SamplerRunnable();
		sampler = new Sampler(samplerRunnable);		
		instrumentationService = new InstrumentationService(instrumentation);
		postService = new PostService(this);

		messageRouter.start();
		sampler.start();
		postService.start();
	}

	public SamplerRunnable getSamplerRunnable() {
		return samplerRunnable;
	}

	@Override
	public void onMessage(Message msg) {
		String command = msg.getType();
		if(JavaAgentMessageType.RESUME.equals(command)) {
			sampler.setRun(true);
		} else if(JavaAgentMessageType.PAUSE.equals(command)) {
			sampler.setRun(false);
		} else if(JavaAgentMessageType.SUBSCRIBE_THREAD_SAMPLING.equals(command)) {
			sampler.setInterval(msg.getIntegerContent());
			sampler.setRun(true);
		} else if(JavaAgentMessageType.UNSUBSCRIBE_THREAD_SAMPLING.equals(command)) {
			sampler.setRun(false);
		} else if(JavaAgentMessageType.INSTRUMENT.equals(command)) {
			InstrumentSubscription subscription = (InstrumentSubscription) msg.getContent();
			instrumentationService.addSubscription(subscription);
		} else if(JavaAgentMessageType.DEINSTRUMENT.equals(command)) {
			InstrumentSubscription subscription = (InstrumentSubscription) msg.getContent();
			instrumentationService.removeSubscription(subscription);
		} else if(JavaAgentMessageType.INSTRUMENT_BATCH_INTERVAL.equals(command)) {
			//agent.getInstrumentationService().setInterval(msg.getIntegerContent());
		}
	}

	public MessageRouter getMessageRouter() {
		return messageRouter;
	}

	@Override
	public void messageRouterDisconnected(MessageRouter router) {
		if(isAlive) {
			isAlive = false;
			close();
			System.out.println("Agent: client disconnected.");
		}
	}

	public boolean isAlive() {
		return isAlive;
	}
	
	public void close() {
		messageRouter.disconnect();
		instrumentationService.destroy();
		sampler.destroy();
	}
}
