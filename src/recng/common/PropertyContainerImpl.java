package recng.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple key/value container backed by a HashMap.
 *
 * @author jon
 * 
 * @param <K>
 *            The generic type of the keys.
 */
public class PropertyContainerImpl<K> implements PropertyContainer<K> {

    private Map<K, Object> properties = new HashMap<K, Object>();

    @Override
    public Object get(K key) {
        return properties.get(key);
    }

    @Override
    public Object set(K key, Object value) {
        return properties.put(key, value);
    }

    @Override
    public <V> V getProperty(K key) {
        Object value = get(key);
        return implicitCast(value);
    }

    @SuppressWarnings("unchecked") private <V> V implicitCast(Object value) {
        return (V)value;
    }

    @Override
    @SuppressWarnings("unchecked") public <V> V setProperty(K key, V value) {
        if (properties == null)
            properties = new HashMap<K, Object>();
        return (V)properties.put(key, value);
    }

    @Override
    public Set<K> getKeys() {
        if (properties == null)
            return Collections.<K>emptySet();
        return new HashSet<K>(properties.keySet());
    }

    @Override
    public boolean containsProperty(K key) {
        return properties.containsKey(key);
    }

    @Override
    public <V> List<V> getRepeatedProperties(K key) {
        return getProperty(key);
    }

    @Override
    public <V> List<V> setRepeatedProperties(K key, List<V> values) {
        List<V> prev = getRepeatedProperties(key);
        setProperty(key, values);
        return prev;
    }

    @Override
    public <V> void addRepeatedProperty(K key, V value) {
        List<V> prev = getRepeatedProperties(key);
        if (prev == null) {
            prev = new ArrayList<V>();
        }
        prev.add(value);
        setRepeatedProperties(key, prev);
    }
}
