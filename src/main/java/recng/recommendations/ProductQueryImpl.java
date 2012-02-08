package recng.recommendations;

import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.filter.ProductFilter;
import recng.recommendations.graph.RecommendationEdgeType;

/**
 * A query used to fetch recommended products.
 * 
 * @author Jon Ivmark
 */
public class ProductQueryImpl implements ProductQuery {

    private final int limit;
    private final RecommendationEdgeType recType;
    private ProductFilter filter;
    private int maxCursorSize;
    private int maxRelationDistance;

    public static final int DEFAULT_MAX_CURSOR_SIZE = 2000;
    public static final int DEFAULT_MAX_RELATION_DISTANCE = 1;

    public ProductQueryImpl(int limit, RecommendationEdgeType recType) {
        this.limit = limit;
        this.recType = recType;
        this.filter = new ProductFilter() {

            @Override
            public boolean accepts(ImmutableProduct product) {
                return true;
            }
        };
        this.maxCursorSize = DEFAULT_MAX_CURSOR_SIZE;
        this.maxRelationDistance = DEFAULT_MAX_RELATION_DISTANCE;
    }

    public void setFilter(ProductFilter filter) {
        this.filter = filter;
    }

    public void setMaxCursorSize(int maxCursorSize) {
        this.maxCursorSize = maxCursorSize;
    }

    public void setMaxRelationDistance(int maxRelationDistance) {
        this.maxRelationDistance = maxRelationDistance;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public ProductFilter getFilter() {
        return filter;
    }

    @Override
    public int getMaxCursorSize() {
        return maxCursorSize;
    }

    @Override
    public int getMaxRelationDistance() {
        return maxRelationDistance;
    }

    @Override
    public RecommendationEdgeType getRecommendationType() {
        return recType;
    }

}
