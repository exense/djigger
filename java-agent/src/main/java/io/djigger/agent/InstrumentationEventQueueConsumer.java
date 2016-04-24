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
import io.djigger.monitoring.java.instrumentation.InstrumentationSample;

import java.util.LinkedList;

import org.smb.core.Message;

public class InstrumentationEventQueueConsumer implements EventQueueConsumer<InstrumentationSample> {
	
	private final AgentSession session;
		
	public InstrumentationEventQueueConsumer(AgentSession session) {
		super();
		this.session = session;
	}

	@Override
	public void processBuffer(LinkedList<InstrumentationSample> buffer) {
		long t1 = System.nanoTime();
		if(buffer.size()>0) {
			t1 = System.nanoTime();
			session.getMessageRouter().send(new Message(JavaAgentMessageType.INSTRUMENT_SAMPLE,buffer));
			System.out.println("Sent "+buffer.size()+" events in "+((System.nanoTime()-t1)/1000000));
		}
	}
}
