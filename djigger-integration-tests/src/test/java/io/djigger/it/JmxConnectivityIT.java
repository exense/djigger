package io.djigger.it;

import io.djigger.client.JMXClientFacade;
import io.djigger.it.support.Await;
import io.djigger.it.support.CollectingFacadeListener;
import io.djigger.it.support.JmxSupport;
import io.djigger.it.support.JvmLauncher;
import io.djigger.it.support.SampleApp;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies JMX connectivity end-to-end: a target JVM ({@link SampleApp}) is launched with the JMX remote
 * agent enabled, and {@link JMXClientFacade} connects to it and collects thread dumps via sampling.
 */
@Tag("integration")
public class JmxConnectivityIT {

    @Test
    public void jmxFacadeConnectsAndReceivesThreadDumps() throws Exception {
        int jmxPort = JvmLauncher.findFreePort();
        Process app = JvmLauncher.launch(SampleApp.class, JmxSupport.remoteArgs(jmxPort), Collections.emptyList());

        JMXClientFacade facade = null;
        try {
            Properties props = new Properties();
            props.setProperty("host", "localhost");
            props.setProperty("port", String.valueOf(jmxPort));

            facade = new JMXClientFacade(props, false);
            CollectingFacadeListener listener = new CollectingFacadeListener();
            facade.addListener(listener);

            assertTrue(JmxSupport.connectWithRetry(facade, 30, 500), "could not establish a JMX connection");
            assertTrue(facade.isConnected(), "JMX facade should be connected");

            facade.setSamplingInterval(200);
            facade.setSampling(true);

            assertTrue(Await.until(() -> !listener.threadInfos.isEmpty(), 15_000),
                    "expected to receive thread dumps via JMX within the timeout");

            // the sample app's worker thread should show up in the JMX thread dump
            boolean workerSeen = listener.threadInfos.stream()
                    .anyMatch(ti -> "djigger-sample-worker".equals(ti.getName()));
            assertTrue(workerSeen, "expected the sample worker thread to appear in the JMX thread dumps");

            facade.setSampling(false);
            // let the last in-flight dump finish while the target JVM and JMX connection are still alive,
            // so the sampler does not run a dump against a torn-down connection (which the Sampler would print)
            Thread.sleep(500);
        } finally {
            if (facade != null) {
                facade.destroy();
            }
            JvmLauncher.stop(app);
        }
    }
}
