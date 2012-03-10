package recng.recommendations;

import java.util.List;

import recng.profiling.Profiler;
import recng.recommendations.domain.ImmutableProduct;

/**
 * Methods for making product recommendations.
 *
 * @author jon
 *
 */
public interface RecommendationModel extends Profiler {

    /**
     * Gets products with a relation to the source product, according to a set
     * of rules defined in a query.
     *
     * @param sourceProduct
     *            The source product
     * @param query
     *            The rules to follow when getting related products
     */
    List<ImmutableProduct> getRelatedProducts(String sourceProduct,
                                              ProductQuery query);


        /**
     * Gets products with a relation to one or more of the source products,
     * following a set of rules defined in a query.
     *
     * @param sourceProducts
     *            The source products
     * @param query
     *            The rules to follow when getting related products
     */
    List<ImmutableProduct> getRelatedProducts(List<String> sourceProducts,
                                              ProductQuery query);

    /**
     * Gets a product based on it's id.
     *
     * @param id
     *            The product id
     * @return The product with the specified properties.
     */
    ImmutableProduct getProduct(String id);

}
