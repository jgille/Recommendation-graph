package recng.recommendations;

import java.util.List;
import java.util.Set;

import recng.common.BinPropertyContainer;
import recng.common.TableMetadata;
import recng.common.WeightedPropertyContainer;

public class ProductImpl<K> implements Product<K> {

    private final K id;
    private final WeightedPropertyContainer<String> properties;

    public ProductImpl(K id, boolean isValid, TableMetadata fields) {
        this.id = id;
        this.properties = new BinPropertyContainer(fields, true);
        setIsValid(isValid);
    }

    public K getId() {
        return id;
    }

    public boolean isValid() {
        Boolean isValid = properties.getProperty(ProductData.IS_VALID_KEY);
        return isValid != null && isValid.booleanValue();
    }

    public void setIsValid(boolean isValid) {
        properties.setProperty(ProductData.IS_VALID_KEY, isValid);
    }

    public <V> V getProperty(String key) {
        return properties.getProperty(key);
    }

    public <V> V setProperty(String key, V value) {
        return properties.setProperty(key, value);
    }

    public boolean containsProperty(String key) {
        return properties.containsProperty(key);
    }

    public Set<String> getKeys() {
        return properties.getKeys();
    }

    public <V> List<V> getRepeatedProperties(String key) {
        return properties.getRepeatedProperties(key);
    }

    public <V> List<V> setRepeatedProperties(String key, List<V> values) {
        return properties.setRepeatedProperties(key, values);
    }

    public <V> void addRepeatedProperty(String key, V value) {
        properties.addRepeatedProperty(key, value);
    }

    public List<String> getCategories() {
        return getRepeatedProperties(ProductData.CATEGORIES_KEY);
    }

    public void setCategories(List<String> categories) {
        properties.setRepeatedProperties(ProductData.CATEGORIES_KEY,
                                         categories);
    }

    public int getWeight() {
        return properties.getWeight();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Id: ").append(id);
        sb.append(". Properties: { ").append(properties).append(" }");
        return sb.toString();
    }
}
