package tests.junit.graph;

import recng.graph.EdgeType;

public enum TestEdgeType implements EdgeType {

    DEFAULT_EDGE_TYPE, SECONDARY_EDGE_TYPE;

    @Override
    public boolean isWeighted() {
        return false;
    }

}
