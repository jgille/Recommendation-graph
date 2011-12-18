package tests.junit.cache;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.cache.Cache;
import recng.cache.CacheBuilder;

/**
 * Tests for {@link recng.cache.ShardedLRUCache}.
 *
 * @author Jon Ivmark
 */
public class TestShardedLRUCache {

    @Test public void testCache() {
        Cache<Integer, String> cache = createCache(1, 1);
        assertEquals(0, cache.size());
        cache.cache(1, "1");
        assertTrue(cache.contains(1));
        assertEquals("1", cache.get(1));
        assertEquals(1, cache.size());

        cache = createCache(2, 1);
        assertEquals(0, cache.size());
        cache.cache(1, "1");
        cache.cache(2, "2");
        assertTrue(cache.contains(1));
        assertEquals("1", cache.get(1));
        assertTrue(cache.contains(2));
        assertEquals("2", cache.get(2));
        assertEquals(2, cache.size());
    }

    @Test public void testEvict() {
        Cache<Integer, String> cache = createCache(2, 1);
        cache.cache(1, "1");
        cache.cache(2, "2");
        cache.evict(1);
        assertFalse(cache.contains(1));
        assertNull(cache.get(1));
        assertTrue(cache.contains(2));
        assertEquals("2", cache.get(2));
        assertEquals(1, cache.size());
    }

    @Test public void testLRUEviction() {
        Cache<Integer, String> cache = createCache(2, 2);
        assertEquals(0, cache.size());
        cache.cache(1, "1");
        cache.cache(2, "2");
        cache.cache(3, "3");
        cache.cache(4, "4");
        cache.cache(5, "5");
        // Since the max size is 2x2, key 1 should bow have been evicted
        assertFalse(cache.contains(1));
        assertNull(cache.get(1));
        assertTrue(cache.contains(2));
        assertEquals("2", cache.get(2));
        assertTrue(cache.contains(3));
        assertEquals("3", cache.get(3));
        assertTrue(cache.contains(4));
        assertEquals("4", cache.get(4));
        assertTrue(cache.contains(5));
        assertEquals("5", cache.get(5));
        assertEquals(4, cache.size());
    }

    @Test public void testNullKey() {
        Cache<Integer, String> cache = createCache(2, 1);
        boolean exception = false;
        try {
            cache.cache(null, "1");
        } catch(IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
        assertEquals(0, cache.size());
    }

    private Cache<Integer, String> createCache(final int shardCount,
                                               int maxSizePerShard) {
        return new CacheBuilder<Integer, String>().maxSize(maxSizePerShard*shardCount).
            concurrencyLevel(shardCount).build();
    }
}
