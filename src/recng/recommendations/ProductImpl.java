package recng.recommendations;

import java.util.List;
import java.util.Set;

import recng.common.BinPropertyContainer;
import recng.common.TableMetadata;
import recng.common.WeightedPropertyContainer;

/**
 * An implementation representing a product and it's properties.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the product IDs.
 */
public class ProductImpl implements Product {

    private final String id;
    private final WeightedPropertyContainer properties;

    /**
     * Constructs a new product.
     *
     * @param id
     *            The product ID
     * @param isValid
     *            Whether or not this product is valid.
     * @param fields
     *            The valid properties for this product.
     */
    public ProductImpl(String id, boolean isValid, TableMetadata fields) {
        this.id = id;
        this.properties = new BinPropertyContainer(fields, true);
        setIsValid(isValid);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isValid() {
        Boolean isValid = properties.getProperty(IS_VALID_PROPERTY);
        return isValid != null && isValid.booleanValue();
    }

    @Override
    public void setIsValid(boolean isValid) {
        properties.setProperty(IS_VALID_PROPERTY, isValid);
    }

    @Override
    public <V> V getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public <V> V setProperty(String key, V value) {
        return properties.setProperty(key, value);
    }

    @Override
    public boolean containsProperty(String key) {
        return properties.containsProperty(key);
    }

    @Override
    public Set<String> getKeys() {
        return properties.getKeys();
    }

    @Override
    public <V> List<V> getRepeatedProperties(String key) {
        return properties.getRepeatedProperties(key);
    }

    @Override
    public <V> List<V> setRepeatedProperties(String key, List<V> values) {
        return properties.setRepeatedProperties(key, values);
    }

    @Override
    public <V> void addRepeatedProperty(String key, V value) {
        properties.addRepeatedProperty(key, value);
    }

    @Override
    public List<String> getCategories() {
        return getRepeatedProperties(CATEGORIES_PROPERTY);
    }

    @Override
    public void setCategories(List<String> categories) {
        properties.setRepeatedProperties(CATEGORIES_PROPERTY, categories);
    }

    @Override
    public int getWeight() {
        return properties.getWeight();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Id: ").append(id);
        sb.append(". Properties: { ").append(properties).append(" }");
        return sb.toString();
    }

    @Override
    public Object get(String key) {
        return properties.get(key);
    }

    @Override
    public Object set(String key, Object value) {
        return properties.set(key, value);
    }
}
