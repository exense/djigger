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
package io.djigger.client;

import io.djigger.model.Connection;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.mbeans.MBeanCollector;
import io.djigger.monitoring.java.mbeans.MBeanCollector.ValueListener;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.sampling.Sampler;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

public class JMXClientFacade extends Facade implements NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(JMXClientFacade.class);

    protected JMXConnector connector;

    protected MBeanCollector mBeanCollector;

    protected volatile ThreadMXBean bean;

    protected final Sampler sampler;

    public JMXClientFacade(Properties properties, boolean autoReconnect) {
        super(properties, autoReconnect);

        final JMXClientFacade me = this;

        sampler = new Sampler(new Runnable() {
            @Override
            public void run() {
                if (me.isConnected() && bean != null) {
                    ThreadInfo[] infos = bean.dumpAllThreads(false, false);

                    List<io.djigger.monitoring.java.model.ThreadInfo> dumps = ThreadDumpHelper.toThreadDump(infos);

                    for (FacadeListener listener : listeners) {
                        listener.threadInfosReceived(dumps);
                    }

                    final List<Metric<?>> metrics = new ArrayList<>();

                    mBeanCollector.collect(new ValueListener() {
                        @Override
                        public void valueReceived(Metric<?> metric) {
                            metrics.add(metric);
                        }
                    });

                    for (FacadeListener listener : listeners) {
                        listener.metricsReceived(metrics);
                    }
                }
            }
        });
        sampler.start();
    }

    @Override
    protected synchronized void destroy_() {
        sampler.destroy();

        if (connector != null) {
            try {
                connector.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        if (notification.getType().equals("jmx.remote.connection.closed") && isConnected()) {
            handleConnectionClosed();
        }
    }


    @Override
    protected void addInstrumentation_(InstrumentSubscription subscription) {
        throw new RuntimeException("Not implemented");
    }


    @Override
    protected void removeInstrumentation_(InstrumentSubscription subscription) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected void startSampling() {
        sampler.setInterval(getSamplingInterval());
        sampler.setRun(true);
    }

    @Override
    protected void stopSampling() {
        sampler.setRun(false);
    }

    @Override
    protected synchronized void connect_() throws Exception {
        String host = properties.getProperty(Connection.Parameters.HOST);
        String port = properties.getProperty(Connection.Parameters.PORT);
        String username = properties.getProperty(Connection.Parameters.USERNAME);
        String password = properties.getProperty(Connection.Parameters.PASSWORD);

        logger.info("Attempting to create JMX connection to " + host + ":" + port);

        String urlPath = "/jndi/rmi://" + host + ":" + port + "/jmxrmi";
        JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);

        Hashtable<String, String[]> h = new Hashtable<String, String[]>();
        if (username != null) {
            String[] credentials = new String[]{username, password};
            h.put("jmx.remote.credentials", credentials);
        }

        connector = JMXConnectorFactory.connect(url, h);
        connector.addConnectionNotificationListener(this, null, null);
        MBeanServerConnection connection = connector.getMBeanServerConnection();

        bean = newPlatformMXBeanProxy(connection, THREAD_MXBEAN_NAME, ThreadMXBean.class);

        mBeanCollector = new MBeanCollector(connection);
        if (metricCollectionConfiguration != null) {
            mBeanCollector.configure(metricCollectionConfiguration.getmBeans());
        }
        
        logger.info("JMX connection to " + host + ":" + port + " created");
    }

    @Override
    public boolean hasInstrumentationSupport() {
        return false;
    }
}
