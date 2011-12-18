package recng.db;

import java.util.Map;

/**
 * Classes used as a key/value store (persistent or RAM only) should implement
 * this interface.
 * 
 * @author Jon Ivmark
 */
public interface KVStore<K, V> {

    void init(Map<String, String> properties);

    V get(K key);

    void put(K key, V value);

    V remove(K key);
}
