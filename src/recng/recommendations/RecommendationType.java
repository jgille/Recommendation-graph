package recng.recommendations;

import recng.graph.EdgeType;

/**
 * Describes the type of a recommendation and an edge type in a product graph.
 *
 * @author jon
 *
 */
public enum RecommendationType implements EdgeType {
    PEOPLE_WHO_BOUGHT,
    PEOPLE_WHO_VIEWED,
    WHOSE_ORDER_INCLUDED;

    @Override
    public boolean isWeighted() {
        return true;
    }
}
