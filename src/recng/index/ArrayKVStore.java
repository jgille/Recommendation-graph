package recng.index;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ArrayKVStore<K, V> {

    public static class Builder<K, V> {
        private final Map<K, V> map;
        private final Comparator<K> comparator;

        public Builder(Comparator<K> comparator) {
        	this.map = new TreeMap<K, V>(comparator);
        	this.comparator = comparator;
        }

        public synchronized Builder<K, V> put(K key, V value) {
        	map.put(key, value);
            return this;
        }

        @SuppressWarnings ("unchecked")
        public synchronized ArrayKVStore<K, V> get() {
                List<K> keys = new ArrayList<K>();
                List<V> values = new ArrayList<V>();
                for(Map.Entry<K, V> e : map.entrySet()) {
                    keys.add(e.getKey());
                    values.add(e.getValue());
                }
                map.clear();
                return new ArrayKVStore<K, V>((K[])keys.toArray(new Object[keys.size()]),
                                              (V[])values.toArray(new Object[values.size()]),
                                              comparator);
            }
    }

    private final K[] keys;
    private final V[] values;
    private final Comparator<K> comparator;

    private ArrayKVStore(K[] keys, V[] values, Comparator<K> comparator) {
        this.keys = keys;
        this.values = values;
        this.comparator = comparator;
    }

    public V find(K key) {
        int index = findKeyIndex(key);
        if(index >= 0)
            return values[index];
        return null;
    }

    private int findKeyIndex(K key) {
        return Arrays.binarySearch(keys, key, comparator);
    }

    public int size() {
        return keys.length;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i]).append("\t").append(values[i]).append("\n");
        }
        return sb.toString();
    }
}