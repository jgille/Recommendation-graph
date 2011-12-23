package recng.recommendations;

import java.util.List;

/**
 * An immutable representation of a product.
 * 
 * @author jon
 * 
 */
public interface ImmutableProduct {

    /**
     * Gets the product ID.
     */
    String getId();

    /**
     * Gets the validity of this product.
     */
    boolean isValid();

    /**
     * Gets the categories for this product.
     */
    List<String> getCategories();

    /**
     * Gets a property by it's key.
     *
     * NOTE: This will throw a CCE if the found value is not an instance of the
     * provided generic type.
     */
    <V> V getProperty(String key);

    /**
     * Gets a repeated property, i.e. a property list, by it's key.
     *
     * NOTE: This will throw a CCE if the found value can not be cast to a list
     * of the provided generic type.
     */
    <V> List<V> getRepeatedProperties(String key);

    /**
     * Check if this container contains the provided key.
     */
    boolean containsProperty(String key);
}