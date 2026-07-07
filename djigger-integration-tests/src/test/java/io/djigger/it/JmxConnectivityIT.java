package io.djigger.it;

import io.djigger.client.JMXClientFacade;
import io.djigger.it.support.CollectingFacadeListener;
import io.djigger.it.support.JvmLauncher;
import io.djigger.it.support.SampleApp;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Verifies JMX connectivity end-to-end: a target JVM ({@link SampleApp}) is launched with the JMX remote
 * agent enabled, and {@link JMXClientFacade} connects to it and collects thread dumps via sampling.
 */
@Tag("integration")
public class JmxConnectivityIT {

    @Test
    public void jmxFacadeConnectsAndReceivesThreadDumps() throws Exception {
        int jmxPort = JvmLauncher.findFreePort();
        Process app = JvmLauncher.launch(SampleApp.class, jmxRemoteArgs(jmxPort), Collections.emptyList());

        JMXClientFacade facade = null;
        try {
            Properties props = new Properties();
            props.setProperty("host", "localhost");
            props.setProperty("port", String.valueOf(jmxPort));

            facade = new JMXClientFacade(props, false);
            CollectingFacadeListener listener = new CollectingFacadeListener();
            facade.addListener(listener);

            connectWithRetry(facade, 30, 500);
            assertTrue(facade.isConnected(), "JMX facade should be connected");

            facade.setSamplingInterval(200);
            facade.setSampling(true);

            assertTrue(waitUntil(() -> !listener.threadInfos.isEmpty(), 15_000),
                    "expected to receive thread dumps via JMX within the timeout");

            // the sample app's worker thread should show up in the JMX thread dump
            boolean workerSeen = listener.threadInfos.stream()
                    .anyMatch(ti -> "djigger-sample-worker".equals(ti.getName()));
            assertTrue(workerSeen, "expected the sample worker thread to appear in the JMX thread dumps");

            facade.setSampling(false);
        } finally {
            if (facade != null) {
                facade.destroy();
            }
            JvmLauncher.stop(app);
        }
    }

    private static List<String> jmxRemoteArgs(int port) {
        return Arrays.asList(
                "-Dcom.sun.management.jmxremote=true",
                "-Dcom.sun.management.jmxremote.port=" + port,
                "-Dcom.sun.management.jmxremote.rmi.port=" + port,
                "-Dcom.sun.management.jmxremote.authenticate=false",
                "-Dcom.sun.management.jmxremote.ssl=false",
                "-Dcom.sun.management.jmxremote.local.only=false",
                "-Djava.rmi.server.hostname=localhost");
    }

    private static void connectWithRetry(JMXClientFacade facade, int attempts, long delayMs) throws InterruptedException {
        for (int i = 0; i < attempts; i++) {
            try {
                facade.connect();
                return;
            } catch (Exception e) {
                Thread.sleep(delayMs);
            }
        }
        fail("could not establish a JMX connection after " + attempts + " attempts");
    }

    private interface Condition {
        boolean met();
    }

    private static boolean waitUntil(Condition condition, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.met()) {
                return true;
            }
            Thread.sleep(100);
        }
        return condition.met();
    }
}
