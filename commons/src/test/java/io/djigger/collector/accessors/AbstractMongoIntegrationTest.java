package io.djigger.collector.accessors;

import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import org.bson.Document;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Base class for MongoDB integration tests. By default these run against the shared central test
 * database (MongoDB 4.4) also used by the step project; every connection parameter can be overridden
 * with a system property so the same suite can be pointed at another server (e.g. a MongoDB 7 instance):
 *
 * <pre>
 *   mvn test -Dmongo.host=localhost -Dmongo.port=27017 -Dmongo.db=djigger_test -Dmongo.user= -Dmongo.password=
 * </pre>
 *
 * If the configured server is unreachable the tests are skipped (assumption failure) rather than failing,
 * so offline builds are not broken.
 *
 * <p>Note: djigger's accessors use fixed collection names ("threaddumps", "stacktraces",
 * "instrumentation", "metrics"). To keep the tests independent, {@link #dropDjiggerCollections()} clears
 * them before and after each test. On a shared server this means concurrent djigger test runs could
 * interfere with each other; the collection names do not clash with the step suite ("beans").
 */
@Tag("mongodb")
public abstract class AbstractMongoIntegrationTest {

    protected static final String HOST = System.getProperty("mongo.host", "central-mongodb.stepcloud-test.ch");
    protected static final int PORT = Integer.getInteger("mongo.port", 27017);
    protected static final String DATABASE = System.getProperty("mongo.db", "test");
    protected static final String USER = System.getProperty("mongo.user", "tester");
    protected static final String PASSWORD = System.getProperty("mongo.password", "5dB(rs+4YRJe");

    protected MongoConnection connection;
    protected MongoDatabase db;

    @BeforeEach
    void connectToMongo() {
        connection = new MongoConnection();
        boolean connected;
        try {
            db = connection.connect(HOST, PORT, emptyToNull(USER), emptyToNull(PASSWORD), DATABASE);
            // force a round-trip so an unreachable/misconfigured server aborts the test rather than failing later
            db.runCommand(new Document("ping", 1));
            connected = true;
        } catch (RuntimeException e) {
            connected = false;
        }
        assumeTrue(connected, () -> "MongoDB not reachable at " + HOST + ":" + PORT + " - skipping integration test");
        dropDjiggerCollections();
    }

    @AfterEach
    void disconnectFromMongo() {
        if (db != null) {
            dropDjiggerCollections();
        }
        if (connection != null) {
            connection.close();
        }
    }

    protected void dropDjiggerCollections() {
        for (String name : new String[]{"threaddumps", "stacktraces", "instrumentation", "metrics"}) {
            db.getCollection(name).drop();
        }
    }

    private static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }
}
