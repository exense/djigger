package io.djigger.client;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import io.denkbar.smb.core.MessageRouter;
import io.djigger.client.util.Jps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessAttachFacade extends AgentFacade {

    private static final Logger logger = LoggerFactory.getLogger(ProcessAttachFacade.class);

    private static final String DEFAULT_CONNECTION_TIMEOUT = "10000";

    public ProcessAttachFacade(Properties properties, boolean autoReconnect) {
        super(properties, autoReconnect);
    }

    @Override
    public void connect_() throws Exception {
        VirtualMachine vm;

        final ServerSocket s = new ServerSocket(0);
        int port = s.getLocalPort();

        final ProcessAttachFacade me = this;
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
                    logger.error("Error while creating facade with properties " + properties, e);
                } finally {
                    try {
                        s.close();
                    } catch (IOException e) {
                    }
                }
            }
        }).start();

        String processID = null;
        Object processNamePattern = properties.get(Parameters.PROCESS_NAME_PATTERN);

        // show a list of all PIDs and displayNames, if needed.
        String listVmsLogLevel = null;

        if (processNamePattern != null) {
            Pattern pattern = Pattern.compile(processNamePattern.toString());
            for (VirtualMachineDescriptor vm_ : VirtualMachine.list()) {
                Matcher matcher = pattern.matcher(vm_.displayName());
                if (matcher.find()) {
                    if (processID != null) {
                        logger.warn("Found multiple PIDs matching process name pattern \""+ processNamePattern+"\" , currently using PID: " + processID);
                        listVmsLogLevel = "warn";
                    }
                    processID = vm_.id();
                }
            }
            if (processID == null) {
                logger.error("No processes found matching process name pattern \"" + processNamePattern + "\"");
                listVmsLogLevel = "error";
            }

            if (listVmsLogLevel != null) {
                String intro = "List of all currently running processes and displayNames (process name patterns):";
                if (listVmsLogLevel.equals("error")) {
                    logger.error(intro);
                    for (String line: Jps.listVms()) {
                        logger.error(line);
                    }
                } else {
                    logger.warn(intro);
                    for (String line: Jps.listVms()) {
                        logger.warn(line);
                    }
                }
            }

            if (processID == null) {
                throw new RuntimeException("No VM found matching pattern " + processNamePattern);
            }
        } else {
            processID = properties.get(Parameters.PROCESS_ID).toString();
        }
        vm = VirtualMachine.attach(processID);

        InputStream is = getClass().getClassLoader().getResourceAsStream("agent.jar");
        File agentJar = File.createTempFile("agent-" + UUID.randomUUID(), ".jar");
        try {
            FileOutputStream os = new FileOutputStream(agentJar);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.flush();
            os.close();

        } catch (IOException e1) {
            logger.error("Error while writing agent temp file to " + agentJar, e1);
        }

        vm.loadAgent(agentJar.getAbsolutePath(), "host:" + Inet4Address.getLocalHost().getHostName() + ",port:" + port);

        long timeout = Long.parseLong(properties.getProperty(Parameters.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT));
        synchronized (this) {
            wait(timeout);
        }

        if (client == null) {
            throw new TimeoutException("Connection timeout (" + timeout + "ms) exceeded");
        }

        startClient();
    }

}
