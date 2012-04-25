package tests.junit.graph;

import recng.graph.*;

/**
 * Tests {@link ImmutableGraphImpl}.
 *
 * @author jon
 *
 */
public class TestImmutableGraphImpl extends AbstractTestGraph {

    @Override
    protected <K> GraphBuilder<K> getGraphBuilder() {
        return ImmutableGraphImpl.Builder.create(getMetadata());
    }

}