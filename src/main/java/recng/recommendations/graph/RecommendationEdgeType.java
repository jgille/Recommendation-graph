package recng.recommendations.graph;

import recng.graph.EdgeType;

/**
 * Describes the type of a recommendation and an edge type in a product graph.
 *
 * @author jon
 *
 */
public enum RecommendationEdgeType implements EdgeType {
    PEOPLE_WHO_BOUGHT(true),
    PEOPLE_WHO_VIEWED(true),
    WHOSE_ORDER_INCLUDED(true),
    BOUGHT(false),
    VIEWED(false);

    private final boolean isWeighted;

    private RecommendationEdgeType(boolean isWeighted) {
        this.isWeighted = isWeighted;
    }

    @Override
    public boolean isWeighted() {
        return isWeighted;
    }
}
