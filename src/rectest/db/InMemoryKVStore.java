package rectest.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple in memory key/value store backed by a HashMap.
 *
 * This class is thread safe.
 *
 * @author Jon Ivmark
 */
public class InMemoryKVStore<K, V> implements KVStore<K, V> {

    private final Map<K, V> store =
        Collections.synchronizedMap(new HashMap<K, V>());

    public void init(Map<String, String> properties) {
        // Ignore
    }

    public V get(K key) {
        return store.get(key);
    }

    public void put(K key, V value) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        store.put(key, value);
    }

    public V remove(K key) {
        return store.remove(key);
    }
}
