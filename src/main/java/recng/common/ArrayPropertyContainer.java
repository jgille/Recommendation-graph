package recng.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * A property container with a fixed set of keys, backed by an array.
 *
 * @author jon
 *
 */
public class ArrayPropertyContainer implements WeightedPropertyContainer {

    private final TableMetadata metadata;
    private final Object[] data;

    public ArrayPropertyContainer(TableMetadata metadata) {
        this.metadata = metadata;
        this.data = new Object[metadata.size()];
    }

    @Override
    public Object getProperty(String key) {
        int index = metadata.ordinal(key);
        if (index < 0)
            throw new IllegalArgumentException("Unknown field: " + key);
        return data[index];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object setProperty(String key, Object value) {
        FieldMetadata fm = metadata.getFieldMetadata(key);
        if (fm == null)
            throw new IllegalArgumentException("Unknown field: " + key);
        if (fm.isRepeated())
            return setRepeatedProperties(key, (List<Object>) value);
        Object prev = getProperty(key);
        data[metadata.ordinal(key)] = value;
        return prev;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> getRepeatedProperties(String key) {
        List<Object> l = (List<Object>) getProperty(key);
        if (l == null)
            return null;
        return new ArrayList<Object>(l);
    }

    @Override
    public List<Object> setRepeatedProperties(String key, List<Object> values) {
        List<Object> prev = getRepeatedProperties(key);
        data[metadata.ordinal(key)] = values != null ? new ArrayList<Object>(values) : null;
        return prev;
    }

    @Override
    public void addRepeatedProperty(String key, Object value) {
        List<Object> prev = getRepeatedProperties(key);
        if (prev == null)
            prev = new ArrayList<Object>();
        prev.add(value);
        setRepeatedProperties(key, prev);
    }

    @Override
    public boolean containsProperty(String key) {
        return metadata.contains(key) && data[metadata.ordinal(key)] != null;
    }

    @Override
    public List<String> getKeys() {
        return metadata.getFields();
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<String, Object>(metadata.size());
        for (String fieldName : metadata.getFields()) {
            Object property = getProperty(fieldName);
            if (property != null)
                map.put(fieldName, property);
        }
        return map;
    }

    @Override
    public int getWeight() {
        int weight = 0;
        int index = 0;
        for (Object obj : data)
            weight += approximateWeight(index++, obj);
        return weight + 24; // summed weights + object and array overhead
    }

    private int approximateWeight(int index, Object obj) {
        return 0; // TODO; Implement
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : getKeys())
            sb.append(key).append(" : ").append(getProperty(key)).append(", ");
        return sb.toString();
    }
}
