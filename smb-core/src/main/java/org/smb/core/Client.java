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
package org.smb.core;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;



public class Client implements MessageRouterStateListener {

	private final String agentHost;

	private final int agentPort;

	private final MessageRouter router;

	private boolean isAlive;

	public Client(String agentHost, int agentPort) throws UnknownHostException, IOException {
		super();
		this.agentHost = agentHost;
		this.agentPort = agentPort;
		this.isAlive = true;
		router = new MessageRouter(this, new Socket(agentHost, agentPort));
		router.start();
	}

	public void sendMessage(String command) throws IOException {
		sendMessage(command, null);
	}

	public void sendMessage(String command, Object content) throws IOException {
		router.send(new Message(command, content));
	}

	public Object call(String command, Object content) throws Exception {
		return router.call(new Message(command, content),60000);
	}

	public String getAgentHost() {
		return agentHost;
	}

	public int getAgentPort() {
		return agentPort;
	}

	public MessageRouter getMessageRouter() {
		return router;
	}

	public void messageRouterDisconnected(MessageRouter router) {
		isAlive = false;
	}

	public boolean isAlive() {
		return isAlive;
	}
	
	public void close() {
		router.disconnect();
		isAlive = false;
	}
}
