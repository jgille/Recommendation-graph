package recng.recommendations;

import recng.recommendations.filter.ProductFilter;
import recng.recommendations.graph.RecommendationEdgeType;

/**
 * A query used to fetch recommended products.
 *
 * @author Jon Ivmark
 */
public interface ProductQuery {

    /**
     * The maximum result size.
     */
    int getLimit();

    /**
     * The filter used to decide if a product should be returned or not.
     */
    ProductFilter getFilter();

    /**
     * The maximum number of products to iterate before returning.
     */
    int getMaxCursorSize();

    /**
     * The maximum "distance" between a source product and a recommended
     * product. The distance is the number of relations that must be traversed
     * to reach a product B from product A.
     *
     * A distance of one will result in only products immediately related to the
     * source product being returned, a distance of two will mean that products
     * related to the products at distance one might also be returned, and so
     * on.
     */
    int getMaxRelationDistance();

    /**
     * The type of recommendation to make.
     */
    RecommendationEdgeType getRecommendationType();
}