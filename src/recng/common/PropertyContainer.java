package recng.common;

import java.util.List;
import java.util.Set;

/**
 * A key/value container.
 *
 * @author jon
 * 
 * @param <K>
 *            The generic type of the keys.
 */
public interface PropertyContainer<K> {

    /**
     * Gets an untyped property by it's key.
     */
    Object get(K key);

    /**
     * Sets a property for a key, and return the previous value.
     */
    Object set(K key, Object value);

    /**
     * Gets a property by it's key.
     *
     * NOTE: This will throw a CCE if the found value is not an instance of the
     * provided generic type.
     */
    <V> V getProperty(K key);

    /**
     * Sets a property for a key, and returns the previous value.
     *
     * NOTE: This will throw a CCE if the found value is not an instance of the
     * provided generic type.
     */
    <V> V setProperty(K key, V value);

    /**
     * Gets a repeated property, i.e. a property list, by it's key.
     *
     * NOTE: This will throw a CCE if the found value can not be cast to a list
     * of the provided generic type.
     */
    <V> List<V> getRepeatedProperties(K key);

    /**
     * Sets a repeated property, i.e. a property list, for a key.
     *
     * NOTE: This will throw a CCE if the found value can not be cast to a list
     * of the provided generic type.
     */
    <V> List<V> setRepeatedProperties(K key, List<V> values);

    /**
     * Appends a property to a repeated property, possibly creating a new
     * repeated property if not found.
     */
    <V> void addRepeatedProperty(K key, V value);

    /**
     * Check if this container contains the provided key.
     */
    boolean containsProperty(K key);

    /**
     * Gets all keys for this container.
     */
    Set<K> getKeys();
}
