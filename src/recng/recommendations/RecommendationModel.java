package recng.recommendations;

import java.util.List;
import java.util.Set;

/**
 * Methods for making product recommendations.
 *
 * @author jon
 *
 */
public interface RecommendationModel<K> {

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
    List<Product<K>> getRelatedProducts(String sourceProduct,
                                        ProductQuery<K> query,
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
    Product<K> getProduct(String id, Set<String> properties);

}
