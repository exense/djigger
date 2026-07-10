package io.djigger.it.support;

import io.djigger.client.Facade;

import java.util.Arrays;
import java.util.List;

/** Helpers for launching a JMX-enabled child JVM and connecting a facade to it. */
public final class JmxSupport {

    private JmxSupport() {
    }

    /** JVM args enabling an unauthenticated JMX remote agent on the given (fixed) port. */
    public static List<String> remoteArgs(int port) {
        return Arrays.asList(
                "-Dcom.sun.management.jmxremote=true",
                "-Dcom.sun.management.jmxremote.port=" + port,
                "-Dcom.sun.management.jmxremote.rmi.port=" + port,
                "-Dcom.sun.management.jmxremote.authenticate=false",
                "-Dcom.sun.management.jmxremote.ssl=false",
                "-Dcom.sun.management.jmxremote.local.only=false",
                "-Djava.rmi.server.hostname=localhost");
    }

    /** Repeatedly attempts to connect the facade (the child JVM's JMX server needs a moment to come up). */
    public static boolean connectWithRetry(Facade facade, int attempts, long delayMs) throws InterruptedException {
        for (int i = 0; i < attempts; i++) {
            try {
                facade.connect();
                return true;
            } catch (Exception e) {
                Thread.sleep(delayMs);
            }
        }
        return false;
    }
}
