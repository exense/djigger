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
