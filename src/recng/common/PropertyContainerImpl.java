package recng.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class PropertyContainerImpl<K> implements PropertyContainer<K> {

    private Map<K, Object> properties = new HashMap<K, Object>();

    public <V> V getProperty(K key) {
        Object value = properties.get(key);
        return implicitCast(value);
    }

    @SuppressWarnings("unchecked") private <V> V implicitCast(Object value) {
        return (V)value;
    }

    @SuppressWarnings("unchecked") public <V> V setProperty(K key, V value) {
        if (properties == null)
            properties = new HashMap<K, Object>();
        return (V)properties.put(key, value);
    }

    public Set<K> getKeys() {
        if (properties == null)
            return Collections.<K>emptySet();
        return new HashSet<K>(properties.keySet());
    }

    public boolean containsProperty(K key) {
        return properties.containsKey(key);
    }

    public <V> List<V> getRepeatedProperties(K key) {
        return getProperty(key);
    }

    public <V> List<V> setRepeatedProperties(K key, List<V> values) {
        List<V> prev = getRepeatedProperties(key);
        setProperty(key, values);
        return prev;
    }

    public <V> void addRepeatedProperty(K key, V value) {
        List<V> prev = getRepeatedProperties(key);
        if (prev == null) {
            prev = new ArrayList<V>();
        }
        prev.add(value);
        setRepeatedProperties(key, prev);
    }
}
