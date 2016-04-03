package io.djigger.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smb.core.MessageRouter;

import com.sun.tools.attach.VirtualMachine;

public class ProcessAttachFacade extends AgentFacade {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessAttachFacade.class);

	public static final String CONNECTION_TIMEOUT = "ConnectionTimeoutMs";
	
	public static final String PROCESSID = "ProcessID";
	
	private static final String DEFAULT_CONNECTION_TIMEOUT = "10000";

	public ProcessAttachFacade(Properties properties, boolean autoReconnect) {
		super(properties, autoReconnect);
	}

	@Override
	public void connect_() throws Exception {
		VirtualMachine vm;

		final ServerSocket s = new ServerSocket(0);
		int port = s.getLocalPort();
		
		final ProcessAttachFacade me=this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Socket socket;
				try {
					socket = s.accept();
					client = new MessageRouter(null, socket);
					synchronized (me) {
						me.notifyAll();
					}
				} catch (IOException e) {
					logger.error("Error while creating facade with properties "+properties, e);
				} finally {
					try {
						s.close();
					} catch (IOException e) {}
				}
			}
		}).start();
		
		vm = VirtualMachine.attach(properties.get(PROCESSID).toString());
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("agent.jar");
		File agentJar = File.createTempFile("agent-"+UUID.randomUUID(),".jar");
		try {
			FileOutputStream os = new FileOutputStream(agentJar);		
			byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = is.read(buffer)) !=-1){
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.flush();
            os.close();
      
		} catch (IOException e1) {
			logger.error("Error while writing agent temp file to "+agentJar, e1);
		}
		
		vm.loadAgent(agentJar.getAbsolutePath(),"host:"+Inet4Address.getLocalHost().getHostName()+",port:"+port);
		
		long timeout = Long.parseLong(properties.getProperty(CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT));
		synchronized (this) {
			wait(timeout);				
		}
		
		if(client==null) {
			throw new TimeoutException("Connection timeout (" + timeout + "ms) exceeded");
		}
		
		startClient();
	}

}
