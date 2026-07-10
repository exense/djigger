package io.djigger.collector.accessors;

import io.djigger.collector.accessors.stackref.ThreadInfoAccessorImpl;
import io.djigger.monitoring.java.model.GlobalThreadId;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises {@link ThreadInfoAccessorImpl} end-to-end against a real MongoDB: index/TTL creation,
 * {@code save}, the migrated {@code countDocuments} path, and the {@code query} read-back (which also
 * covers the ThreadInfo &lt;-&gt; Document mapping and the stacktrace de-duplication).
 */
public class ThreadInfoAccessorIntegrationTest extends AbstractMongoIntegrationTest {

    private ThreadInfoAccessorImpl accessor;

    @BeforeEach
    void initAccessor() {
        accessor = new ThreadInfoAccessorImpl(db);
        accessor.createIndexesIfNeeded(3600L);
    }

    @Test
    public void createsTtlIndexOnTimestamp() {
        boolean ttlIndexPresent = false;
        for (Document index : db.getCollection("threaddumps").listIndexes()) {
            Object key = index.get("key");
            if (key instanceof Document && ((Document) key).containsKey("timestamp")
                    && index.containsKey("expireAfterSeconds")) {
                ttlIndexPresent = true;
            }
        }
        assertTrue(ttlIndexPresent, "expected a TTL index on the 'timestamp' field");
    }

    @Test
    public void createIndexesIsIdempotentWhenTtlIndexAlreadyExists() {
        // The TTL index was already created with 3600s in @BeforeEach. Calling createIndexesIfNeeded again
        // exercises the "index already exists" branch that reads expireAfterSeconds - the branch that used to
        // throw ClassCastException on MongoDB 7 (Integer vs Long). It must be a no-op here.
        accessor.createIndexesIfNeeded(3600L);
        assertEquals(3600L, ttlSecondsOf("threaddumps", "timestamp"));
    }

    @Test
    public void updatesTtlWhenValueChanges() {
        accessor.createIndexesIfNeeded(7200L);
        assertEquals(7200L, ttlSecondsOf("threaddumps", "timestamp"));
    }

    @Test
    public void dropsTtlWhenCleared() {
        accessor.createIndexesIfNeeded(null);
        assertNull(ttlSecondsOf("threaddumps", "timestamp"), "the TTL should have been removed");
        assertTrue(hasIndexOn("threaddumps", "timestamp"), "a plain index on timestamp should remain");
    }

    private Long ttlSecondsOf(String collection, String field) {
        for (Document index : db.getCollection(collection).listIndexes()) {
            Object key = index.get("key");
            if (key instanceof Document && ((Document) key).containsKey(field)) {
                Object value = index.get("expireAfterSeconds");
                return value instanceof Number ? ((Number) value).longValue() : null;
            }
        }
        return null;
    }

    private boolean hasIndexOn(String collection, String field) {
        for (Document index : db.getCollection(collection).listIndexes()) {
            Object key = index.get("key");
            if (key instanceof Document && ((Document) key).containsKey(field)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void savesAndQueriesThreadInfoRoundTrip() throws Exception {
        long now = System.currentTimeMillis();
        accessor.save(threadInfo(now, "worker-1", "S01"));
        accessor.save(threadInfo(now, "worker-2", "S01"));

        Date from = new Date(now - 60_000);
        Date to = new Date(now + 60_000);

        assertEquals(2, accessor.count(null, from, to, 5, TimeUnit.SECONDS));

        // filter by a stored attribute and read back
        List<ThreadInfo> result = new ArrayList<>();
        accessor.query(new Document("env", "S01"), from, to).forEach(result::add);
        assertEquals(2, result.size());

        ThreadInfo read = result.stream().filter(t -> "worker-1".equals(t.getName())).findFirst().orElseThrow();
        assertEquals(Thread.State.RUNNABLE, read.getState());
        assertEquals("runtime-1", read.getGlobalId().getRuntimeId());
        assertEquals(1, read.getStackTrace().length);
        StackTraceElement el = read.getStackTrace()[0];
        assertEquals("com.example.Foo", el.getClassName());
        assertEquals("bar", el.getMethodName());
        assertEquals(42, el.getLineNumber());
    }

    @Test
    public void countIsZeroOutsideTimeWindow() throws Exception {
        long now = System.currentTimeMillis();
        accessor.save(threadInfo(now, "worker-1", "S01"));

        Date from = new Date(now + 120_000);
        Date to = new Date(now + 180_000);
        assertEquals(0, accessor.count(null, from, to, 5, TimeUnit.SECONDS));
    }

    private static ThreadInfo threadInfo(long timestamp, String name, String env) {
        StackTraceElement[] stack = new StackTraceElement[]{
                new StackTraceElement("com.example.Foo", "bar", "Foo.java", 42)
        };
        ThreadInfo info = new ThreadInfo(stack, new GlobalThreadId("runtime-1", 7L), timestamp);
        info.setName(name);
        info.setState(Thread.State.RUNNABLE);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("env", env);
        info.setAttributes(attributes);
        return info;
    }
}
