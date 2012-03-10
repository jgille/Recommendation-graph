package recng.common;

import java.util.List;
import java.util.Map;

/**
 * A key/value container.
 * 
 * @author jon
 */
public interface PropertyContainer {

    /**
     * Gets a property for a key.
     */
    Object getProperty(String key);

    /**
     * Sets a property for a key.
     */
    Object setProperty(String key, Object value);

    /**
     * Gets repeated properties, i.e. a list of properties, for a key.
     */
    List<Object> getRepeatedProperties(String key);

    /**
     * Sets repeated properties, i.e. a list of properties, for a key.
     */
    List<Object> setRepeatedProperties(String key, List<Object> values);

    /**
     * Adds a property to a set of repeated properties.
     */
    void addRepeatedProperty(String key, Object value);

    /**
     * Checks the existance of a key.
     */
    boolean containsProperty(String key);

    /**
     * Gets all keys for this container.
     */
    List<String> getKeys();

    /**
     * Gets properties as a map.
     */
    Map<String, Object> asMap();
}
