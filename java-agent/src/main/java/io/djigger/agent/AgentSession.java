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

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import io.denkbar.smb.core.Message;
import io.denkbar.smb.core.MessageListener;
import io.denkbar.smb.core.MessageRouter;
import io.denkbar.smb.core.MessageRouterStateListener;
import io.djigger.monitoring.eventqueue.EventQueue;
import io.djigger.monitoring.eventqueue.EventQueue.EventQueueConsumer;
import io.djigger.monitoring.eventqueue.ModuloEventSkipLogic;
import io.djigger.monitoring.java.agent.JavaAgentMessageType;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.monitoring.java.sampling.Sampler;

public class AgentSession implements MessageListener, MessageRouterStateListener {

	private final MessageRouter messageRouter;

	private volatile boolean isAlive;

	private final Sampler sampler;

	private final InstrumentationService instrumentationService;

	private final EventQueue<InstrumentationEvent> instrumentationEventQueue;

	private final EventQueue<ThreadInfo> threadInfoQueue;
	
	private final EventQueue<Metric<?>> metricsQueue;

	public AgentSession(Socket socket, Instrumentation instrumentation) throws IOException {
		super();

		this.messageRouter = new MessageRouter(this, socket);
		messageRouter.registerPermanentListenerForAllMessages(this);
		this.isAlive = true;

		EventQueueConsumer<InstrumentationEvent> queueConsumer = new InstrumentationEventQueueConsumer(this);
		instrumentationEventQueue = new EventQueue<InstrumentationEvent>(1, TimeUnit.SECONDS, queueConsumer, new ModuloEventSkipLogic<InstrumentationEvent>() {
			@Override
			protected long getSkipAttribute(InstrumentationEvent object) {
				return object.getStart();
			}
		});
		InstrumentationEventCollector.setEventCollector(instrumentationEventQueue);

		EventQueueConsumer<ThreadInfo> threadInfoQueueConsumer = new ThreadInfoEventQueueConsumer(this);
		threadInfoQueue = new EventQueue<ThreadInfo>(1, TimeUnit.SECONDS, threadInfoQueueConsumer, new ModuloEventSkipLogic<ThreadInfo>() {
			@Override
			protected long getSkipAttribute(ThreadInfo object) {
				return object.getTimestamp();
			}
		});
		
		metricsQueue = new EventQueue<Metric<?>>(1, TimeUnit.SECONDS, new EventQueueConsumer<Metric<?>>() {
			@Override
			public void processBuffer(LinkedList<Metric<?>> collector) {
				messageRouter.send(new Message(JavaAgentMessageType.METRICS, collector));
			}
		},new ModuloEventSkipLogic<Metric<?>>() {
			@Override
			protected long getSkipAttribute(Metric<?> object) {
				return object.getTime();
			}
		});

		sampler = new Sampler(new SamplerRunnable(threadInfoQueue, metricsQueue));
		instrumentationService = new InstrumentationService(instrumentation);

		messageRouter.start();
		sampler.start();
	}

	@Override
	public void onMessage(Message msg) {
		String command = msg.getType();
		if (JavaAgentMessageType.RESUME.equals(command)) {
			sampler.setRun(true);
		} else if (JavaAgentMessageType.PAUSE.equals(command)) {
			sampler.setRun(false);
		} else if (JavaAgentMessageType.SUBSCRIBE_THREAD_SAMPLING.equals(command)) {
			sampler.setInterval(msg.getIntegerContent());
			sampler.setRun(true);
		} else if (JavaAgentMessageType.UNSUBSCRIBE_THREAD_SAMPLING.equals(command)) {
			sampler.setRun(false);
		} else if (JavaAgentMessageType.INSTRUMENT.equals(command)) {
			InstrumentSubscription subscription = (InstrumentSubscription) msg.getContent();
			instrumentationService.addSubscription(subscription);
		} else if (JavaAgentMessageType.DEINSTRUMENT.equals(command)) {
			InstrumentSubscription subscription = (InstrumentSubscription) msg.getContent();
			instrumentationService.removeSubscription(subscription);
		} else if (JavaAgentMessageType.INSTRUMENT_BATCH_INTERVAL.equals(command)) {
			// agent.getInstrumentationService().setInterval(msg.getIntegerContent());
		}
	}

	public MessageRouter getMessageRouter() {
		return messageRouter;
	}

	public InstrumentationService getInstrumentationService() {
		return instrumentationService;
	}

	@Override
	public void messageRouterDisconnected(MessageRouter router) {
		if (isAlive) {
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
		instrumentationEventQueue.shutdown();
		threadInfoQueue.shutdown();
	}
}
