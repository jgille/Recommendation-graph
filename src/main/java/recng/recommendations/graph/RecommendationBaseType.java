package recng.recommendations.graph;

import recng.graph.EdgeType;

/**
 * Descibes edges used in graphs that are the base data for product -> product
 * recommendation graphs, e.g. edges between users and products (bought) and
 * sessions and products (viewed).
 * 
 * @author jon
 * 
 */
public enum RecommendationBaseType implements EdgeType {

    BOUGHT, VIEWED;

    @Override
    public boolean isWeighted() {
        return false;
    }
}
