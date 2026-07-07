package io.djigger.collector.accessors;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link MongoConnection} (migrated to mongodb-driver-sync 5.x) can connect to and
 * authenticate against a real server, using the modern {@code MongoClients.create}/{@code MongoClientSettings}
 * path and the {@code ping}-based connectivity check.
 */
public class MongoConnectionIntegrationTest extends AbstractMongoIntegrationTest {

    @Test
    public void connectsAndPings() {
        assertNotNull(db);
        Document ping = db.runCommand(new Document("ping", 1));
        assertTrue(ping.getDouble("ok") == 1.0d, () -> "ping did not return ok=1: " + ping.toJson());
    }

    @Test
    public void reportsServerVersion() {
        Document buildInfo = db.runCommand(new Document("buildInfo", 1));
        String version = buildInfo.getString("version");
        assertNotNull(version);
        System.out.println("Connected to MongoDB " + version + " at " + HOST + ":" + PORT);
    }
}
