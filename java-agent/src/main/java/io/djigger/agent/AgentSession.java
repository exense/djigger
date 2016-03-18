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
