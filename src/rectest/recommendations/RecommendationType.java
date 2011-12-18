package rectest.recommendations;

import rectest.graph.EdgeType;

public enum RecommendationType implements EdgeType {
    PEOPLE_WHO_BOUGHT,
    PEOPLE_WHO_VIEWED,
    WHOSE_ORDER_INCLUDED;

    @Override
    public boolean isWeighted() {
        return true;
    }
}
