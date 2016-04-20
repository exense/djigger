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

import io.djigger.agent.Collector;
import io.djigger.monitoring.java.agent.JavaAgentMessageType;
import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

import org.smb.core.Message;

public class PostService extends Thread {
	
	private int interval;
	
	private final AgentSession session;
	
	public PostService(AgentSession session) {
		super();
		this.session = session;
		interval = 1000;
	}

	@Override
	public void run() {
		List<InstrumentationSample> buffer;
		List<ThreadInfo> samplerBuffer;
		boolean skipNext=false;
		while(true) {
			long start = System.currentTimeMillis();
			try {
				buffer = new ArrayList<InstrumentationSample>();
				Collector.drainTo(buffer);
				
				samplerBuffer = new ArrayList<ThreadInfo>();
				session.getSamplerRunnable().drainTo(samplerBuffer);

				if(!skipNext) {
					if(buffer.size()>0) {
						session.getMessageRouter().send(new Message(JavaAgentMessageType.INSTRUMENT_SAMPLE,buffer));
					}
					if(samplerBuffer.size()>0) {
						session.getMessageRouter().send(new Message(JavaAgentMessageType.THREAD_SAMPLE,samplerBuffer));
					}
				} else {
					System.out.println("Skipping "+buffer.size()+" samples and "+samplerBuffer.size()+" dumps.");					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				long duration = System.currentTimeMillis()-start;
				if(duration>interval) {
					skipNext = true;
				} else {
					skipNext = false;
				}
				try {
					Thread.sleep(Math.max(0, interval-duration));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

		}
	}

}
