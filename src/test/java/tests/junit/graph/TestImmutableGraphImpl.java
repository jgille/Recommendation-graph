package tests.junit.graph;

import recng.graph.*;
import recng.recommendations.graph.RecommendationGraphMetadata;

/**
 * Tests {@link ImmutableGraphImpl}.
 *
 * @author jon
 *
 */
public class TestImmutableGraphImpl extends AbstractTestGraph {

    @Override
    protected <K> GraphBuilder<K> getGraphBuilder() {
        return ImmutableGraphImpl.Builder.create(RecommendationGraphMetadata
            .getInstance());
    }
}