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

import io.djigger.monitoring.eventqueue.EventQueue.EventQueueConsumer;
import io.djigger.monitoring.java.agent.JavaAgentMessageType;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smb.core.Message;

public class InstrumentationEventQueueConsumer implements EventQueueConsumer<InstrumentationEvent> {
	
	private static final Logger logger = LoggerFactory.getLogger(InstrumentationEventQueueConsumer.class);
	
	private final AgentSession session;
		
	public InstrumentationEventQueueConsumer(AgentSession session) {
		super();
		this.session = session;
	}

	@Override
	public void processBuffer(LinkedList<InstrumentationEvent> buffer) {
		if(buffer.size()>0) {
			long t1 = System.nanoTime();
			session.getMessageRouter().send(new Message(JavaAgentMessageType.INSTRUMENT_SAMPLE,buffer));
			if(logger.isDebugEnabled()) {
				logger.debug("Sent "+buffer.size()+" instrumentation events in "+((System.nanoTime()-t1)/1000000));				
			}
		}
	}
}
