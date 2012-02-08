package recng.recommendations.domain;

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
public interface Product extends WeightedPropertyContainer {
    /** The field name used to store the validity property. */
    public static final String ID_PROPERTY = "__id";
    /** The field name used to store the validity property. */
    public static final String IS_VALID_PROPERTY = "__is_valid";
    /** The field name used to store the categories property. */
    public static final String CATEGORIES_PROPERTY = "__categories";

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
