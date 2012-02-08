package recng.recommendations;

import java.util.List;
import java.util.Map;

import recng.recommendations.domain.ImmutableProduct;

/**
 * The manager interface for making recommendations.
 * 
 * @author jon
 * 
 */
public interface RecommendationManager {

    /**
     * Makes a product recommendation.
     * 
     * @param params
     *            A map of input parameters.
     */
    List<ImmutableProduct> getRelatedProducts(Map<String, String> params);

}
