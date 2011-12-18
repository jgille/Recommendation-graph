package rectest.common;

import java.util.List;
import java.util.Set;

public interface PropertyContainer<K> {

    <V> V getProperty(K key);

    <V> V setProperty(K key, V value);

    <V> List<V> getRepeatedProperties(K key);

    <V> List<V> setRepeatedProperties(K key, List<V> values);

    <V> void addRepeatedProperty(K key, V value);

    boolean containsProperty(K key);

    Set<K> getKeys();
}
