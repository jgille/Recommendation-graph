package tests.junit.graph;

import rectest.graph.*;

public class TestGraphImpl extends AbstractTestGraph {

    @Override
    protected <K> GraphBuilder<K> getGraphBuilder() {
        return new GraphImpl.Builder<K>();
    }
}