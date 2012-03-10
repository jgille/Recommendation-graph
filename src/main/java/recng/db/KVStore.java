package recng.db;

import java.util.Map;

/**
 * Classes used as a key/value store (persistent or RAM only) should implement
 * this interface.
 * 
 * @author Jon Ivmark
 */
public interface KVStore<K, V> {

    /**
     * Initiates the store with any properties that is needed.
     */
    void init(Map<String, String> properties);

    /**
     * Gets a value by key.
     */
    V get(K key);

    /**
     * Stores a key/value pair.
     */
    void put(K key, V value);

    /**
     * Removes a key/value pair.
     */
    V remove(K key);
}
