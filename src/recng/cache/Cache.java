package recng.cache;

/**
 * Classes used to store cached values should implement this interface.
 *
 * @author Jon Ivmark
 */
public interface Cache<K, V> {

    /**
     * Caches a value.
     */
    V cache(K key, V value);

    /**
     * Checks if the cache contains the given key.
     */
    boolean contains(K key);

    /**
     * Removes a cached entry.
     */
    V evict(K key);

    /**
     * Gets a cached entry.
     */
    V get(K key);

    /**
     * Gets the number of cached entries.
     */
    int size();

    /**
     * Removes all entries from the cache.
     */
    void clear();
}
