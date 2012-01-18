package recng.recommendations.domain;

import java.util.ArrayList;
import java.util.List;
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
        this.properties =
            new BinPropertyContainer.Factory(false).create(fields);
        setIsValid(isValid);
    }

    private <E> E getAndCast(String field) {
        return implicitCast(properties.getProperty(field));
    }

    @Override
    public boolean isValid() {
        Boolean isValid = getAndCast(IS_VALID_PROPERTY);
        return isValid == null || isValid.booleanValue();
    }

    @Override
    public void setIsValid(boolean isValid) {
        properties.setProperty(IS_VALID_PROPERTY, isValid);
    }

    @Override
    public Object getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Object setProperty(String key, Object value) {
        return properties.setProperty(key, value);
    }

    @Override
    public boolean containsProperty(String key) {
        return properties.containsProperty(key);
    }

    @Override
    public List<String> getKeys() {
        return properties.getKeys();
    }

    @Override
    public List<Object> getRepeatedProperties(String key) {
        return properties.getRepeatedProperties(key);
    }

    @Override
    public List<Object> setRepeatedProperties(String key, List<Object> values) {
        return properties.setRepeatedProperties(key, values);
    }

    @Override
    public void addRepeatedProperty(String key, Object value) {
        properties.addRepeatedProperty(key, value);
    }

    @Override
    public List<String> getCategories() {
        return implicitCast(getRepeatedProperties(CATEGORIES_PROPERTY));
    }

    @Override
    public void setCategories(List<String> categories) {
        List<Object> catList = new ArrayList<Object>();
        for (String category : categories)
            catList.add(category);
        properties.setRepeatedProperties(CATEGORIES_PROPERTY, catList);
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

    @SuppressWarnings("unchecked")
    private static <E> E implicitCast(Object value) {
        return (E) value;
    }
}
