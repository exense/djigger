package io.djigger.collector.accessors.stackref;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LRUCacheTest {

    @Test
    public void evictsLeastRecentlyUsedEntry() {
        LRUCache<String, Integer> cache = new LRUCache<>(3);
        cache.put("a", 1);
        cache.put("b", 2);
        assertEquals(2, cache.size());

        // access "a" so that "b" becomes the least-recently-used entry
        cache.get("a");

        // inserting a third entry trips removeEldestEntry (size() >= cacheSize) and evicts the LRU entry
        cache.put("c", 3);

        assertEquals(2, cache.size());
        assertTrue(cache.containsKey("a"));
        assertFalse(cache.containsKey("b"), "the least-recently-used entry should have been evicted");
        assertTrue(cache.containsKey("c"));
    }
}
