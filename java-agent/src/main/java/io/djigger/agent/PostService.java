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

import io.djigger.Collector;
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
		while(true) {
			try {				
				buffer = new ArrayList<InstrumentationSample>();
				Collector.drainTo(buffer);
				if(buffer.size()>0) {
					session.getMessageRouter().send(new Message(JavaAgentMessageType.INSTRUMENT_SAMPLE,buffer));
				}

				
				samplerBuffer = new ArrayList<ThreadInfo>();
				session.getSamplerRunnable().drainTo(samplerBuffer);
				if(samplerBuffer.size()>0) {
					long t1 = System.currentTimeMillis();
					session.getMessageRouter().send(new Message(JavaAgentMessageType.THREAD_SAMPLE,samplerBuffer));
					System.out.println("Sent dumps in: " + Long.toString(System.currentTimeMillis()-t1));
				}
				
				Thread.sleep(interval);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
