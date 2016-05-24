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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.smb.core.Message;

import io.djigger.monitoring.java.agent.JavaAgentMessageType;

public class Agent extends Thread {
	
	private final static Integer DEFAULT_PORT = 12121;

	private final boolean agentToClientConnection;
	
	private final Integer port;
	
	private final String clientHost;

	private AgentSession session;
	
	private final Instrumentation instrumentation;

	public static void agentmain(String agentArgs, Instrumentation inst) {
		premain(agentArgs, inst);
	}
	
	
	public static void premain(String agentArguments, Instrumentation instrumentation) {
		System.out.println("Starting agent. Arguments: " + agentArguments);
		
		Map<String,String> parameterMap = new HashMap<String,String>();

		try {
			if(agentArguments!=null) {
				String[] split = agentArguments.split(",");
				for(String argument:split) {
					Matcher matcher = Pattern.compile("(.+?):(.+?)$").matcher(argument);
					if(matcher.find()) {
						parameterMap.put(matcher.group(1), matcher.group(2));
					}
				}
			}

			if (parameterMap.containsKey("host")) {
				String clientHost = parameterMap.get("host");
				Integer port = Integer.decode(parameterMap.get("port"));
				(new Agent(instrumentation, clientHost, port)).start();
			} else if(parameterMap.containsKey("port")) {
				Integer port = Integer.decode(parameterMap.get("port"));
				(new Agent(instrumentation, port)).start();
			} else {
				Integer port = DEFAULT_PORT;
				(new Agent(instrumentation, port)).start();
			}
		} catch(Exception e) {
			System.out.println("Agent: an error occurred while trying to parse the agent parameters.");
			e.printStackTrace();
		}
	}

	private Agent(Instrumentation instrumentation, Integer port) {
		super();
		this.agentToClientConnection = false;
		if(port==null) {
			port = DEFAULT_PORT;
		}
		this.port = port;
		this.clientHost = null;
		this.instrumentation = instrumentation;

		addCollectorToBootstrap(instrumentation);
	}
	
	private Agent(Instrumentation instrumentation, String host, Integer port) {
		super();
		this.agentToClientConnection = true;
		this.port = port;
		this.clientHost = host;
		this.instrumentation = instrumentation;

		addCollectorToBootstrap(instrumentation);
	}

	private void addCollectorToBootstrap(Instrumentation instrumentation) {
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("collector.jar");
			File collectorJar = File.createTempFile("collector-"+UUID.randomUUID(),".jar");
			System.out.println("Adding " + collectorJar + " to bootstrap search.");
			FileOutputStream os = new FileOutputStream(collectorJar);		
			byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = is.read(buffer)) !=-1){
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.flush();
            os.close();
            
			instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(collectorJar));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public AgentSession getSession() {
		return session;
	}

	@Override
	public void run() {
		if(agentToClientConnection) {
			connectToClient();
		} else {
			startServerSocket();						
		}
	}


	private void connectToClient() {
		try {
			Socket socket = new Socket(clientHost, port);
			new AgentSession(socket, instrumentation);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void startServerSocket() {
		try {
			System.out.println("Agent: starting server socket on port "+ port);
			ServerSocket serverSocket = new ServerSocket(port);
			while(true) {
				Socket socket = serverSocket.accept();
				AgentSession session = new AgentSession(socket, instrumentation);
				if(this.session == null || (this.session!=null && !this.session.isAlive())) {
					this.session = session;
				} else {
					session.getMessageRouter().send(new Message(JavaAgentMessageType.MAX_AGENT_SESSIONS_REACHED,null));
					session.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
