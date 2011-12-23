package recng.recommendations;

import java.util.List;
import java.util.Set;

/**
 * Methods for making product recommendations.
 * 
 * @author jon
 * 
 * @param <T>
 *            The generic type of the internally stored product IDs.
 */
public interface RecommendationModel<T> {

    /**
     * Gets products with a relation to the source product, according to a set
     * of rules defined in a query.
     *
     * @param sourceProduct
     *            The source product
     * @param query
     *            The rules to follow when getting related products
     * @param properties
     *            The set of properties to include for the returned products
     */
    List<Product<T>> getRelatedProducts(String sourceProduct,
                                        ProductQuery<T> query,
                                        Set<String> properties);

    /**
     * Gets a product based on it's id.
     *
     * @param id
     *            The product id
     * @param properties
     *            The set of properties to get for the product
     * @return The product with the specified properties.
     */
    Product<T> getProduct(String id, Set<String> properties);

}
