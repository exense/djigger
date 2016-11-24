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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageRouter extends Thread {
	
	private final static Logger logger = LoggerFactory.getLogger(MessageRouter.class);
	
	private final static String ALL_MESSAGES_LISTENER = "##all##";

	private final ConcurrentHashMap<Integer, SynchronMessageResponseHolder> register = new ConcurrentHashMap<Integer, SynchronMessageResponseHolder>();

	private final ConcurrentHashMap<String, List<MessageListener>> permanentRegister = new ConcurrentHashMap<String, List<MessageListener>>();
	
	private final ConcurrentHashMap<String, SynchronMessageListener> synchronListenerRegister = new ConcurrentHashMap<String, SynchronMessageListener>();

	private final AtomicInteger seq = new AtomicInteger();

	private ExecutorService executor = Executors.newFixedThreadPool(2);

	private final Socket socket;
	
	private final ObjectOutputStream out;

	private final ObjectInputStream in;
	
	private final MessageRouterStateListener stateListener;
	
	private boolean connected;

	public MessageRouter(String host, Integer port) throws UnknownHostException, IOException {
		this(null, new Socket(host, port));
	}
	
	public MessageRouter(MessageRouterStateListener listener, String host, Integer port) throws UnknownHostException, IOException {
		this(listener, new Socket(host, port));
	}
	
	public MessageRouter(MessageRouterStateListener listener, Socket socket) throws IOException {
		super();
		this.socket = socket;
		this.out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		this.out.flush();
		this.in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		this.connected = true;
		this.stateListener = listener;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Message message = null;
				try {
					message = (Message) in.readObject();
				} catch (ClassNotFoundException e) {
					handleException(e);
				}
				if(message!=null) {
					try {
						dispatch(message);
					} catch (Exception e) {
						logger.error("Error while dispatching message " + message.getType(), e);
					}
				}
			}
		} catch (Exception e) {
			handleException(e);
		}
	}

	private void handleException(Exception e) {
		connected = false;
		if(!(e instanceof SocketException)) {
			logger.error("Unexpected error", e);
		}
		try {
			in.close();
		} catch (IOException e2) {}
		try {
			out.close();
		} catch (IOException e1) {}
		if(stateListener!=null) {
			stateListener.messageRouterDisconnected(this);
		}
	}

	private void dispatch(Message m) {
		if(m instanceof SynchronMessage) {
			if(m instanceof SynchronMessageResponse) {
				SynchronMessageResponse response = (SynchronMessageResponse) m;
				SynchronMessageResponseHolder responseHolder = register.remove(response.getCorrelationID());
				if(responseHolder!=null) {
					synchronized (responseHolder) {
						responseHolder.processed = true;
						responseHolder.response = m.getContent();
						responseHolder.notify();					
					}
				}
			
			} else {
				SynchronMessage message = (SynchronMessage) m;
				SynchronMessageListener listener = synchronListenerRegister.get(message.getType());
				if(listener!=null) {
					executor.submit(new CallSynchronListenerTask(listener,message));
				}
			}
		} else {			
			submitCallListenerTask(m, permanentRegister.get(m.getType()));
			submitCallListenerTask(m, permanentRegister.get(ALL_MESSAGES_LISTENER));
		}
	}

	private void submitCallListenerTask(Message m, List<MessageListener> listeners) {
		if(listeners!=null) {
			for(MessageListener listener:listeners) {
				executor.submit(new CallListenerTask(listener,m));
			}
		}
	}
	
	public void sendMessage(String command) throws IOException {
		sendMessage(command, null);
	}

	public void sendMessage(String command, Object content) throws IOException {
		send(new Message(command, content));
	}

	public void send(Message message) {
		synchronized(out) {
			try {
				out.writeObject(message);
				out.reset();
				out.flush();
			} catch (IOException e) {
				handleException(e);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
	}

	public Object call(Message message, long timeout) throws Exception {
		int correlationID = seq.incrementAndGet();
		
		SynchronMessageResponseHolder responseHholder = new SynchronMessageResponseHolder();
		SynchronMessage synchronMessage = new SynchronMessage(message.getType(), message.getContent(), correlationID);
		
		register.put(correlationID, responseHholder);
		send(synchronMessage);
		
		synchronized(responseHholder) {
			if(!responseHholder.processed) {
				responseHholder.wait(timeout);
			}
		}
		
		if(responseHholder.processed) {
			if(responseHholder.exception!=null) {
				throw responseHholder.exception;
			} else {
				return responseHholder.response;				
			}
		} else {
			throw new TimeoutException("Timeout occurred while calling " + message.getType());
		}

	}

	public synchronized void registerPermanentListener(String type, MessageListener listener) {
		if(!permanentRegister.containsKey(type)) {
			permanentRegister.put(type, new ArrayList<MessageListener>());
		}
		permanentRegister.get(type).add(listener);
	}
	
	public synchronized void registerPermanentListenerForAllMessages(MessageListener listener) {
		registerPermanentListener(ALL_MESSAGES_LISTENER, listener);
	}
	
	public synchronized void registerSynchronListener(String type, SynchronMessageListener listener) {
		if(!synchronListenerRegister.containsKey(type)) {
			synchronListenerRegister.put(type, listener);
		} else {
			throw new RuntimeException("Only one SynchronMessageListener can be registered. A message listener is already registered for the message type " + type);
		}
	}
	
	public synchronized void unregisterPermanentListener(String type, MessageListener listener) {
		if(permanentRegister.containsKey(type)) {
			permanentRegister.get(type).remove(listener);
		}
	}

	public void disconnect() {
		try {
			in.close();
		} catch (IOException e2) {}
		try {
			out.close();
		} catch (IOException e1) {}
		try {
			socket.close();
		} catch (IOException e) {}
		executor.shutdownNow();
	}

	private class CallListenerTask implements Runnable {

		private final MessageListener listener;

		private final Message msg;

		public CallListenerTask(MessageListener listener, Message msg) {
			super();
			this.listener = listener;
			this.msg = msg;
		}

		public void run() {
			try {
				logger.debug("Received message: "+ msg.getType());
				listener.onMessage(msg);
			} catch (Exception e) {
				logger.error("Error while calling listener " + listener.getClass().getCanonicalName(), e);
			}
		}

	}
	
	private class CallSynchronListenerTask implements Runnable {

		private final SynchronMessageListener listener;

		private final SynchronMessage msg;

		public CallSynchronListenerTask(SynchronMessageListener listener, SynchronMessage msg) {
			super();
			this.listener = listener;
			this.msg = msg;
		}

		public void run() {
			Serializable reponse = null;
			Exception exception = null;
			try {
				System.out.println("Received message: "+ msg.getType());
				reponse = listener.onSynchronMessage(msg);
			} catch (Exception e) {
				exception = e;
			} finally {
				SynchronMessageResponse response = new SynchronMessageResponse(msg.getType(), reponse, msg.getCorrelationID(), exception);
				send(response);
			}
		}

	}

	private class SynchronMessageResponseHolder {
		
		public boolean processed = false;
		
		public Object response;
		
		public Exception exception;

	}
	
	public boolean isConnected() {
		return connected;
	}


}
