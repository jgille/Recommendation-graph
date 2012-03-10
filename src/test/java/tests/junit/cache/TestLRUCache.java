package tests.junit.cache;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.cache.Cache;
import recng.cache.CacheBuilder;
import recng.cache.Weigher;

/**
 * Tests for {@link recng.cache.LRUCache}.
 * 
 * @author Jon Ivmark
 */
public class TestLRUCache {

    @Test
    public void testCache() {
        Cache<Integer, String> cache =
            new CacheBuilder<Integer, String>().maxSize(1).concurrencyLevel(1).build();
        assertEquals(0, cache.size());
        cache.cache(1, "1");
        assertTrue(cache.contains(1));
        assertEquals("1", cache.get(1));
        assertEquals(1, cache.size());
    }

    @Test
    public void testEvict() {
        Cache<Integer, String> cache =
            new CacheBuilder<Integer, String>().maxSize(1).concurrencyLevel(1).build();
        cache.cache(1, "1");
        assertEquals("1", cache.evict(1));
        assertFalse(cache.contains(1));
        assertNull(cache.get(1));
        assertEquals(0, cache.size());
    }

    @Test
    public void testLRUEviction() {
        Cache<Integer, String> cache =
            new CacheBuilder<Integer, String>().maxSize(1).concurrencyLevel(1).build();
        cache.cache(1, "1");
        cache.cache(2, "2");
        assertFalse(cache.contains(1));
        assertNull(cache.get(1));
        assertTrue(cache.contains(2));
        assertEquals("2", cache.get(2));
        assertEquals(1, cache.size());
    }

    @Test
    public void testWeightEviction() {
        Cache<Integer, String> cache =
            new CacheBuilder<Integer, String>().concurrencyLevel(1).
                maxWeight(10).weigher(new Weigher<Integer, String>() {
                    public int weigh(int overhead, Integer key, String value) {
                        return key.intValue();
                    }
                }).build();
        cache.cache(1, "1");
        cache.cache(4, "4");
        assertTrue(cache.contains(1));
        assertEquals("1", cache.get(1));
        assertTrue(cache.contains(4));
        assertEquals("4", cache.get(4));
        assertEquals(2, cache.size());

        cache.cache(6, "6");
        assertFalse(cache.contains(1));
        assertNull(cache.get(1));
        assertTrue(cache.contains(4));
        assertEquals("4", cache.get(4));
        assertTrue(cache.contains(6));
        assertEquals("6", cache.get(6));
        assertEquals(2, cache.size());
    }

    @Test
    public void testNullKey() {
        Cache<Integer, String> cache =
            new CacheBuilder<Integer, String>().maxSize(1).concurrencyLevel(1).build();
        boolean exception = false;
        try {
            cache.cache(null, "1");
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
        assertEquals(0, cache.size());
    }
}
