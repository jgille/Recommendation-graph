package recng.db;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple in memory key/value store.
 *
 * This class is thread safe.
 *
 * @author Jon Ivmark
 */
public class InMemoryKVStore<K, V> implements KVStore<K, V> {

    // Key -> index in data list
    private final TObjectIntMap<K> index = new TObjectIntHashMap<K>();
    // All value entries
    private final List<V> data = new ArrayList<V>();

    public void init(Map<String, String> properties) {
        // Ignore
    }

    public synchronized V get(K key) {
        return getValue(getIndex(key));
    }

    public synchronized void put(K key, V value) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        int idx = getIndex(key);
        if (idx < 0) {
            idx = data.size();
            data.add(value);
            index.put(key, idx);
        } else {
            data.set(idx, value);
        }
    }

    public synchronized V remove(K key) {
        V res = get(key);
        int idx = index.remove(key);
        if (idx >= 0)
            data.set(idx, null);
        return res;
    }

    private int getIndex(K key) {
        if (!index.containsKey(key))
            return -1;
        return index.get(key);
    }

    private V getValue(int index) {
        if (index < 0)
            return null;
        if (index >= data.size())
            throw new IllegalArgumentException("Illegal index: " + index +
                " (>= " + data.size() + ")");
        return data.get(index);
    }
}
