package io.djigger.it;

import io.djigger.client.AgentFacade;
import io.djigger.it.support.Await;
import io.djigger.it.support.CollectingFacadeListener;
import io.djigger.it.support.JmxSupport;
import io.djigger.it.support.JvmLauncher;
import io.djigger.it.support.ServletWorkload;
import io.djigger.monitoring.java.instrumentation.subscription.ServletTracer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Covers djigger's {@code ServletTracer} (migrated to the {@code jakarta.servlet} namespace) end-to-end:
 * a target JVM ({@link ServletWorkload}) repeatedly invokes {@code jakarta.servlet.Servlet.service(...)}
 * with the djigger agent attached; an {@link AgentFacade} applies the tracer and the test asserts that
 * instrumentation events for the {@code service} method are captured.
 *
 * <p>Migrated from the former manual {@code djigger-demo} {@code ServletTracerTest}. This also guards the
 * javax → jakarta migration of {@code ServletTracer}: it matches classes directly implementing
 * {@code jakarta.servlet.Servlet}. Skipped when the agent jar is unavailable. The workload runs on a
 * minimal classpath (its own class + jakarta.servlet-api, no djigger jars; see the module README).
 */
@Tag("integration")
public class ServletTracerIT {

    @Test
    public void servletTracerInstrumentsServletService() throws Exception {
        File agentJar = locateAgentJar();
        assumeTrue(agentJar != null && agentJar.isFile(),
                "agent jar not found (property agent.jar) - skipping servlet tracer test");

        // Handshake file: the workload only loads its servlet once this file appears, which the test
        // creates after applying the subscription (so the servlet is instrumented at class-load time -
        // ServletTracer matches on class load, not on retransformation of an already-loaded class).
        File goFile = File.createTempFile("servlet-tracer-go", ".txt");
        assertTrue(goFile.delete(), "could not prepare the handshake file");

        int agentPort = JvmLauncher.findFreePort();
        String classpath = JvmLauncher.classpathOf(ServletWorkload.class, jakarta.servlet.Servlet.class);
        List<String> jvmArgs = Collections.singletonList("-javaagent:" + agentJar.getAbsolutePath() + "=port:" + agentPort);
        Process app = JvmLauncher.launch(ServletWorkload.class, classpath, jvmArgs,
                Collections.singletonList(goFile.getAbsolutePath()));

        AgentFacade facade = null;
        try {
            Properties props = new Properties();
            props.setProperty("host", "localhost");
            props.setProperty("port", String.valueOf(agentPort));

            facade = new AgentFacade(props, false);
            CollectingFacadeListener listener = new CollectingFacadeListener();
            facade.addListener(listener);

            assertTrue(JmxSupport.connectWithRetry(facade, 30, 500), "could not connect to the djigger agent");
            ServletTracer tracer = new ServletTracer();
            facade.addInstrumentation(tracer);
            int subscriptionId = tracer.getId();

            // addInstrumentation is asynchronous (the INSTRUMENT message is processed on the agent side);
            // give it a moment to register before signalling the workload, so the servlet is instrumented
            // at class-load time rather than missing the subscription and relying on retransformation.
            Thread.sleep(2000);
            assertTrue(goFile.createNewFile(), "could not create the handshake file");

            boolean serviceCaptured = Await.until(() -> listener.instrumentationEvents.stream()
                    .anyMatch(e -> e.getSubscriptionID() == subscriptionId && "service".equals(e.getMethodname())), 30_000);
            assertTrue(serviceCaptured,
                    "expected 'service' instrumentation events from the ServletTracer (jakarta.servlet); "
                            + "events=" + listener.instrumentationEvents
                            + " errors=" + listener.instrumentationErrors);

            facade.setSampling(false);
            Thread.sleep(500);
        } finally {
            if (facade != null) {
                facade.destroy();
            }
            JvmLauncher.stop(app);
            goFile.delete();
        }
    }

    private static File locateAgentJar() {
        String path = System.getProperty("agent.jar");
        if (path != null) {
            return new File(path);
        }
        File fallback = new File("target/agent.jar");
        return fallback.isFile() ? fallback : null;
    }
}
