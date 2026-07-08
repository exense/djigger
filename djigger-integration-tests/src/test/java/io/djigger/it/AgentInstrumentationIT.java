package io.djigger.it;

import com.mongodb.client.MongoDatabase;
import io.djigger.client.mbeans.MetricCollectionConfiguration;
import io.djigger.collector.accessors.MongoConnection;
import io.djigger.collector.server.Server;
import io.djigger.collector.server.conf.CollectorConfig;
import io.djigger.collector.server.conf.CollectorConfigs;
import io.djigger.collector.server.conf.ConnectionsConfig;
import io.djigger.it.support.Await;
import io.djigger.it.support.JvmLauncher;
import io.djigger.it.support.SampleApp;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.subscription.SimpleSubscription;
import io.djigger.monitoring.java.mbeans.MBeanCollectorConfiguration;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Full end-to-end test of the agent's instrumentation and MBean-metric capabilities: a target JVM runs
 * {@link SampleApp} with the djigger agent attached; a collector connects with an instrumentation
 * subscription on {@code SampleApp.businessMethod} and an MBean metric-collection configuration. The test
 * verifies that instrumentation events for the method and the collected metric are stored into MongoDB.
 *
 * <p>Runs against the same central test MongoDB as the other integration tests (overridable via {@code -Dmongo.*})
 * and is skipped when the agent jar or the database is not available.
 */
@Tag("integration")
public class AgentInstrumentationIT {

    private static final String MONGO_HOST = System.getProperty("mongo.host", "central-mongodb.stepcloud-test.ch");
    private static final int MONGO_PORT = Integer.getInteger("mongo.port", 27017);
    private static final String MONGO_DB = System.getProperty("mongo.db", "test");
    private static final String MONGO_USER = System.getProperty("mongo.user", "tester");
    private static final String MONGO_PASSWORD = System.getProperty("mongo.password", "5dB(rs+4YRJe");

    @Test
    public void agentInstrumentationAndMetricsAreStoredByTheCollector() throws Exception {
        File agentJar = locateAgentJar();
        assumeTrue(agentJar != null && agentJar.isFile(),
                "agent jar not found (property agent.jar) - skipping agent instrumentation test");

        MongoConnection probe = tryConnect();
        assumeTrue(probe != null, "MongoDB not reachable - skipping agent instrumentation test");

        int agentPort = JvmLauncher.findFreePort();
        Process targetApp = null;
        Server collector = new Server();
        try {
            dropDjiggerCollections(probe.getDb());

            List<String> jvmArgs = Collections.singletonList("-javaagent:" + agentJar.getAbsolutePath() + "=port:" + agentPort);
            targetApp = JvmLauncher.launch(SampleApp.class, JvmLauncher.codeSourceOf(SampleApp.class), jvmArgs, Collections.emptyList());

            // instrument SampleApp.businessMethod and collect the java.lang:type=Memory MBean
            List<InstrumentSubscription> subscriptions = new ArrayList<>();
            subscriptions.add(new SimpleSubscription("io.djigger.it.support.SampleApp", "businessMethod", true));

            MBeanCollectorConfiguration mbeans = new MBeanCollectorConfiguration();
            mbeans.addMBeanAttribute("java.lang:type=Memory");
            MetricCollectionConfiguration metrics = new MetricCollectionConfiguration();
            metrics.setmBeans(mbeans);

            CollectorConfig config = CollectorConfigs.collectorConfig(
                    MONGO_HOST, MONGO_PORT, MONGO_USER, MONGO_PASSWORD, MONGO_DB, 3600L);
            ConnectionsConfig connections = CollectorConfigs.singleAgentConnection(agentPort, 200, subscriptions, metrics);
            collector.startCollector(config, connections);

            MongoDatabase db = probe.getDb();
            boolean instrumentationStored = Await.until(
                    () -> db.getCollection("instrumentation").countDocuments(eq("method", "businessMethod")) > 0, 60_000);
            assertTrue(instrumentationStored,
                    "expected instrumentation events for SampleApp.businessMethod to be stored by the collector");

            boolean metricsStored = Await.until(
                    () -> db.getCollection("metrics").countDocuments(eq("name", "java.lang/type=Memory")) > 0, 60_000);
            assertTrue(metricsStored, "expected the java.lang:type=Memory metric to be stored by the collector");
        } finally {
            // Stop the collector while the agent is still alive so Server.stop() can drain in-flight writes.
            collector.stop();
            JvmLauncher.stop(targetApp);
            try {
                dropDjiggerCollections(probe.getDb());
            } finally {
                probe.close();
            }
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

    private static MongoConnection tryConnect() {
        try {
            MongoConnection connection = new MongoConnection();
            MongoDatabase db = connection.connect(MONGO_HOST, MONGO_PORT, emptyToNull(MONGO_USER), emptyToNull(MONGO_PASSWORD), MONGO_DB);
            db.runCommand(new Document("ping", 1));
            return connection;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void dropDjiggerCollections(MongoDatabase db) {
        for (String name : new String[]{"threaddumps", "stacktraces", "instrumentation", "metrics"}) {
            db.getCollection(name).drop();
        }
    }

    private static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }
}
