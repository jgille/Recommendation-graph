package recng.cache;

/**
 * Classes used to weigh key/value pairs in a cache should implement this interface.
 *
 * @author Jon Ivmark
 */
public interface Weigher<K, V> {
    /**
     * Weighs an entry in the cache.
     *
     * @param overhead The approximate memory overhead (in bytes) for an entry in the cache.
     * @param key The key of the entry.
     * @param Value The value of the entry.
     */
    int weigh(int overhead, K key, V value);
}
