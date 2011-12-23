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
 */
public class PropertyContainerImpl implements PropertyContainer {

    private Map<String, Object> properties = new HashMap<String, Object>();

    @Override
    public Object get(String key) {
        return properties.get(key);
    }

    @Override
    public Object set(String key, Object value) {
        return properties.put(key, value);
    }

    @Override
    public <V> V getProperty(String key) {
        Object value = get(key);
        return implicitCast(value);
    }

    @SuppressWarnings("unchecked") private <V> V implicitCast(Object value) {
        return (V)value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V setProperty(String key, V value) {
        if (properties == null)
            properties = new HashMap<String, Object>();
        return (V)properties.put(key, value);
    }

    @Override
    public Set<String> getKeys() {
        if (properties == null)
            return Collections.<String> emptySet();
        return new HashSet<String>(properties.keySet());
    }

    @Override
    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public <V> List<V> getRepeatedProperties(String key) {
        return getProperty(key);
    }

    @Override
    public <V> List<V> setRepeatedProperties(String key, List<V> values) {
        List<V> prev = getRepeatedProperties(key);
        setProperty(key, values);
        return prev;
    }

    @Override
    public <V> void addRepeatedProperty(String key, V value) {
        List<V> prev = getRepeatedProperties(key);
        if (prev == null) {
            prev = new ArrayList<V>();
        }
        prev.add(value);
        setRepeatedProperties(key, prev);
    }
}
