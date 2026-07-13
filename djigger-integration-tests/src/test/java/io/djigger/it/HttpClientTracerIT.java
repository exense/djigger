package io.djigger.it;

import com.sun.net.httpserver.HttpServer;
import io.djigger.client.AgentFacade;
import io.djigger.it.support.Await;
import io.djigger.it.support.CollectingFacadeListener;
import io.djigger.it.support.HttpWorkload;
import io.djigger.it.support.JmxSupport;
import io.djigger.it.support.JvmLauncher;
import io.djigger.monitoring.java.instrumentation.subscription.HttpClientTracer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Covers djigger's {@code HttpClientTracer} end-to-end: a target JVM ({@link HttpWorkload}) issues Apache
 * HttpClient requests with the djigger agent attached; an {@link AgentFacade} applies the tracer, which
 * injects a {@code "djigger"} correlation header into outgoing requests. A lightweight in-process HTTP
 * server receives the requests and the test asserts that the injected header arrives.
 *
 * <p>Migrated from the former manual {@code djigger-demo} {@code HttpClientTracerTest}. The receiving
 * server is the JDK's {@link HttpServer} (no servlet container needed). Skipped when the agent jar is
 * unavailable. The workload runs on a minimal classpath (its own class + Apache HttpClient jars only, no
 * djigger jars) so the shaded agent can instrument it without a classpath collision (see the module README).
 */
@Tag("integration")
public class HttpClientTracerIT {

    @Test
    public void httpClientTracerInjectsCorrelationHeader() throws Exception {
        File agentJar = locateAgentJar();
        assumeTrue(agentJar != null && agentJar.isFile(),
                "agent jar not found (property agent.jar) - skipping HTTP client tracer test");

        AtomicBoolean sawDjiggerHeader = new AtomicBoolean(false);
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            if (exchange.getRequestHeaders().containsKey("djigger")) {
                sawDjiggerHeader.set(true);
            }
            byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        String url = "http://localhost:" + server.getAddress().getPort() + "/";

        int agentPort = JvmLauncher.findFreePort();
        String classpath = JvmLauncher.classpathOf(
                HttpWorkload.class,
                Class.forName("org.apache.http.impl.client.HttpClients"),
                Class.forName("org.apache.http.impl.DefaultBHttpClientConnection"),
                Class.forName("org.apache.http.HttpHost"),
                Class.forName("org.apache.commons.logging.LogFactory"),
                Class.forName("org.apache.commons.codec.binary.Base64"));
        List<String> jvmArgs = Collections.singletonList("-javaagent:" + agentJar.getAbsolutePath() + "=port:" + agentPort);
        Process app = JvmLauncher.launch(HttpWorkload.class, classpath, jvmArgs, Collections.singletonList(url));

        AgentFacade facade = null;
        try {
            Properties props = new Properties();
            props.setProperty("host", "localhost");
            props.setProperty("port", String.valueOf(agentPort));

            facade = new AgentFacade(props, false);
            facade.addListener(new CollectingFacadeListener());

            assertTrue(JmxSupport.connectWithRetry(facade, 30, 500), "could not connect to the djigger agent");
            facade.addInstrumentation(new HttpClientTracer());

            assertTrue(Await.until(sawDjiggerHeader::get, 30_000),
                    "expected the HttpClientTracer to inject a 'djigger' header into the outgoing request");

            facade.setSampling(false);
            Thread.sleep(500);
        } finally {
            if (facade != null) {
                facade.destroy();
            }
            JvmLauncher.stop(app);
            server.stop(0);
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
