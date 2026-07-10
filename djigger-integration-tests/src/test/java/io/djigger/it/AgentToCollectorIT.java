package io.djigger.it;

import com.mongodb.client.MongoDatabase;
import io.djigger.collector.accessors.MongoConnection;
import io.djigger.collector.server.Server;
import io.djigger.collector.server.conf.CollectorConfig;
import io.djigger.collector.server.conf.CollectorConfigs;
import io.djigger.collector.server.conf.ConnectionsConfig;
import io.djigger.it.support.JvmLauncher;
import io.djigger.it.support.SampleApp;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Full end-to-end test: a target JVM ({@link SampleApp}) runs with the djigger agent attached; a collector
 * is started programmatically, connects to the agent, samples thread dumps and stores them into MongoDB.
 * The test then verifies that thread dumps land in the database.
 *
 * <p>Runs against the same central test MongoDB as the unit integration tests (overridable via {@code -Dmongo.*}).
 * It is skipped when the agent jar or the database is not available.
 */
@Tag("integration")
public class AgentToCollectorIT {

    private static final String MONGO_HOST = System.getProperty("mongo.host", "central-mongodb.stepcloud-test.ch");
    private static final int MONGO_PORT = Integer.getInteger("mongo.port", 27017);
    private static final String MONGO_DB = System.getProperty("mongo.db", "test");
    private static final String MONGO_USER = System.getProperty("mongo.user", "tester");
    private static final String MONGO_PASSWORD = System.getProperty("mongo.password", "5dB(rs+4YRJe");

    @Test
    public void agentThreadDumpsAreStoredByTheCollector() throws Exception {
        File agentJar = locateAgentJar();
        assumeTrue(agentJar != null && agentJar.isFile(),
                "agent jar not found (property agent.jar) - skipping agent e2e test");

        MongoConnection probe = tryConnect();
        assumeTrue(probe != null, "MongoDB not reachable - skipping agent e2e test");

        int agentPort = JvmLauncher.findFreePort();
        Process targetApp = null;
        Server collector = new Server();
        try {
            dropDjiggerCollections(probe.getDb());

            // start the target application with the djigger agent listening on agentPort
            List<String> jvmArgs = Collections.singletonList("-javaagent:" + agentJar.getAbsolutePath() + "=port:" + agentPort);
            targetApp = JvmLauncher.launch(SampleApp.class, JvmLauncher.codeSourceOf(SampleApp.class), jvmArgs, Collections.emptyList());

            // start the collector: connect to the agent, sample every 200ms and store to MongoDB
            CollectorConfig config = CollectorConfigs.collectorConfig(
                    MONGO_HOST, MONGO_PORT, MONGO_USER, MONGO_PASSWORD, MONGO_DB, 3600L);
            ConnectionsConfig connections = CollectorConfigs.singleAgentConnection(agentPort, 200);
            collector.startCollector(config, connections);

            // the collector facade reconnects on a timer (first attempt ~10s), so allow a generous timeout
            boolean stored = waitUntil(() -> probe.getDb().getCollection("threaddumps").countDocuments() > 0, 60_000);
            long count = probe.getDb().getCollection("threaddumps").countDocuments();
            assertTrue(stored, "expected the collector to store thread dumps sampled from the agent, but found " + count);
        } finally {
            // Stop the collector while the agent is still alive: Server.stop() first tells the agent to stop
            // sampling and lets in-flight writes drain, so shutdown does not interrupt an ongoing Mongo write.
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
            db.runCommand(new org.bson.Document("ping", 1));
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

    private interface Condition {
        boolean met();
    }

    private static boolean waitUntil(Condition condition, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.met()) {
                return true;
            }
            Thread.sleep(250);
        }
        return condition.met();
    }

    private static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }
}
