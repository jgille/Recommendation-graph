package recng.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * A simple key/value container backed by a HashMap.
 *
 * Note that this class is not thread safe.
 * 
 * @author jon
 * 
 */
public class PropertyContainerImpl implements PropertyContainer {

    private final Map<String, Object> properties = new HashMap<String, Object>();

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Object setProperty(String key, Object value) {
        return properties.put(key, value);
    }
    @Override
    public List<String> getKeys() {
        if (properties == null)
            return Collections.<String> emptyList();
        return new ArrayList<String>(properties.keySet());
    }

    @Override
    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> getRepeatedProperties(String key) {
        Object prop = getProperty(key);
        if (prop == null)
            return null;
        if (!(prop instanceof List))
            throw new IllegalArgumentException("Can not cast " + prop
                + " to a list.");
        return (List<Object>) prop;
    }

    @Override
    public List<Object> setRepeatedProperties(String key, List<Object> values) {
        List<Object> prev = getRepeatedProperties(key);
        setProperty(key, values);
        return prev;
    }

    @Override
    public void addRepeatedProperty(String key, Object value) {
        List<Object> prev = getRepeatedProperties(key);
        if (prev == null) {
            prev = new ArrayList<Object>();
        }
        prev.add(value);
        setRepeatedProperties(key, prev);
    }

    @Override
    public Map<String, Object> asMap() {
        return new HashMap<String, Object>(properties);
    }
}
