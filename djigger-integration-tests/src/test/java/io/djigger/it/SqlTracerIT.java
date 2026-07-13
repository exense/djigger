package io.djigger.it;

import io.djigger.client.AgentFacade;
import io.djigger.it.support.Await;
import io.djigger.it.support.CollectingFacadeListener;
import io.djigger.it.support.JmxSupport;
import io.djigger.it.support.JvmLauncher;
import io.djigger.it.support.SqlWorkload;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.subscription.SQLPreparedStatementTracer;
import io.djigger.monitoring.java.instrumentation.subscription.SQLStatementTracer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Covers djigger's SQL tracers ({@code SQLStatementTracer} / {@code SQLPreparedStatementTracer}) end-to-end:
 * a target JVM ({@link SqlWorkload}) runs JDBC statements against in-memory HSQLDB with the djigger agent
 * attached; an {@link AgentFacade} applies the SQL tracer subscriptions and the test asserts that
 * instrumentation events for the {@code execute*} methods are captured.
 *
 * <p>Migrated from the former manual {@code djigger-demo} {@code SQLTracerTest}. No MongoDB is involved -
 * events are asserted directly on the facade listener. Skipped when the agent jar is unavailable.
 *
 * <p>The workload is launched on a <em>minimal</em> classpath (its own class + the HSQLDB driver) so its
 * instrumented JDBC classes resolve djigger types only from the shaded agent (see the module README).
 */
@Tag("integration")
public class SqlTracerIT {

    @Test
    public void sqlTracersInstrumentJdbcStatements() throws Exception {
        File agentJar = locateAgentJar();
        assumeTrue(agentJar != null && agentJar.isFile(),
                "agent jar not found (property agent.jar) - skipping SQL tracer test");

        int agentPort = JvmLauncher.findFreePort();
        String classpath = JvmLauncher.classpathOf(SqlWorkload.class, Class.forName("org.hsqldb.jdbc.JDBCDriver"));
        List<String> jvmArgs = Collections.singletonList("-javaagent:" + agentJar.getAbsolutePath() + "=port:" + agentPort);
        Process app = JvmLauncher.launch(SqlWorkload.class, classpath, jvmArgs, Collections.emptyList());

        AgentFacade facade = null;
        try {
            Properties props = new Properties();
            props.setProperty("host", "localhost");
            props.setProperty("port", String.valueOf(agentPort));

            facade = new AgentFacade(props, false);
            CollectingFacadeListener listener = new CollectingFacadeListener();
            facade.addListener(listener);

            assertTrue(JmxSupport.connectWithRetry(facade, 30, 500), "could not connect to the djigger agent");

            facade.addInstrumentation(new SQLStatementTracer());
            facade.addInstrumentation(new SQLPreparedStatementTracer());

            boolean sqlCaptured = Await.until(() -> listener.instrumentationEvents.stream()
                    .anyMatch(e -> isExecuteMethod(e.getMethodname())), 30_000);
            assertTrue(sqlCaptured, "expected SQL execute* instrumentation events from the SQL tracers, got: "
                    + methodsSeen(listener.instrumentationEvents));

            facade.setSampling(false);
            Thread.sleep(500);
        } finally {
            if (facade != null) {
                facade.destroy();
            }
            JvmLauncher.stop(app);
        }
    }

    private static boolean isExecuteMethod(String method) {
        return "execute".equals(method) || "executeQuery".equals(method) || "executeUpdate".equals(method);
    }

    private static String methodsSeen(List<InstrumentationEvent> events) {
        return events.stream().map(InstrumentationEvent::getMethodname).distinct().sorted().collect(java.util.stream.Collectors.joining(", "));
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
