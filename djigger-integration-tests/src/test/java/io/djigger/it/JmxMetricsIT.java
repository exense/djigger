package io.djigger.it;

import io.djigger.client.JMXClientFacade;
import io.djigger.client.mbeans.MetricCollectionConfiguration;
import io.djigger.it.support.Await;
import io.djigger.it.support.CollectingFacadeListener;
import io.djigger.it.support.JmxSupport;
import io.djigger.it.support.JvmLauncher;
import io.djigger.it.support.SampleApp;
import io.djigger.monitoring.java.mbeans.MBeanCollectorConfiguration;
import io.djigger.monitoring.java.model.Metric;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies MBean metric collection over JMX: the {@link JMXClientFacade} is configured to collect the
 * {@code java.lang:type=Memory} MBean of a target JVM and the sampled metrics are delivered to the listener.
 */
@Tag("integration")
public class JmxMetricsIT {

    @Test
    public void jmxFacadeCollectsMBeanMetrics() throws Exception {
        int jmxPort = JvmLauncher.findFreePort();
        Process app = JvmLauncher.launch(SampleApp.class, JmxSupport.remoteArgs(jmxPort), Collections.emptyList());

        JMXClientFacade facade = null;
        try {
            Properties props = new Properties();
            props.setProperty("host", "localhost");
            props.setProperty("port", String.valueOf(jmxPort));

            facade = new JMXClientFacade(props, false);

            // configure metric collection BEFORE connecting: connect_ wires the MBeanCollector from this config
            MBeanCollectorConfiguration mbeans = new MBeanCollectorConfiguration();
            mbeans.addMBeanAttribute("java.lang:type=Memory");
            MetricCollectionConfiguration metricConfig = new MetricCollectionConfiguration();
            metricConfig.setmBeans(mbeans);
            facade.setMetricCollectionConfiguration(metricConfig);

            CollectingFacadeListener listener = new CollectingFacadeListener();
            facade.addListener(listener);

            assertTrue(JmxSupport.connectWithRetry(facade, 30, 500), "could not establish a JMX connection");

            facade.setSamplingInterval(200);
            facade.setSampling(true);

            boolean memoryMetricSeen = Await.until(() -> listener.metrics.stream()
                    .anyMatch(m -> "java.lang/type=Memory".equals(m.getName())), 15_000);
            assertTrue(memoryMetricSeen, "expected a java.lang:type=Memory metric to be collected via JMX");

            // the collected metric carries a non-null value (a GenericObject of MBean attributes)
            Metric<?> memory = listener.metrics.stream()
                    .filter(m -> "java.lang/type=Memory".equals(m.getName()))
                    .findFirst().orElseThrow();
            assertTrue(memory.getValue() != null, "the memory metric should carry a value");

            facade.setSampling(false);
            Thread.sleep(500);
        } finally {
            if (facade != null) {
                facade.destroy();
            }
            JvmLauncher.stop(app);
        }
    }
}
