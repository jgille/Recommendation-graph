package tests.junit.graph;

import recng.graph.*;
import recng.recommendations.graph.RecommendationGraphMetadata;

public class TestGraphImpl extends AbstractTestGraph {

    @Override
    protected <K> GraphBuilder<K> getGraphBuilder() {
        return new ImmutableGraphImpl.Builder<K>(
                                        RecommendationGraphMetadata
                                            .getInstance());
    }
}