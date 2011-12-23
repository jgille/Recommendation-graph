package recng.recommendations;

import java.util.List;

import recng.common.WeightedPropertyContainer;

/**
 * A product and it's properties.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the product ID.
 */
public interface Product<T> extends WeightedPropertyContainer<String> {

    /** The field name used to store the validity property. */
    public static final String IS_VALID_PROPERTY = "__IS_VALID";
    /** The field name used to store the categories property. */
    public static final String CATEGORIES_PROPERTY = "__CATEGORIES";

    /**
     * Gets the product ID.
     */
    T getId();

    /**
     * Gets the validity of this product.
     */
    boolean isValid();

    /**
     * Sets the validity of this product.
     */
    void setIsValid(boolean isValid);

    /**
     * Gets the categories for this product.
     */
    List<String> getCategories();

    /**
     * Sets the categories for this product.
     */
    void setCategories(List<String> categories);
}
