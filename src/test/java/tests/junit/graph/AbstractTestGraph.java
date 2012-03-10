package tests.junit.graph;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import recng.graph.*;
import recng.recommendations.domain.RecommendationNodeType;
import recng.recommendations.graph.RecommendationEdgeType;

/**
 * Base class for testing {@link Graph}s.
 *
 * @author jon
 *
 */
public abstract class AbstractTestGraph {

    private static final RecommendationEdgeType DEFAULT_EDGE_TYPE =
        RecommendationEdgeType.PEOPLE_WHO_BOUGHT;
    private static final RecommendationEdgeType SECONDARY_EDGE_TYPE =
        RecommendationEdgeType.PEOPLE_WHO_VIEWED;

    private static final NodeType NODE_TYPE = RecommendationNodeType.PRODUCT;

    private static final Integer N1 = 1;
    private static final Integer N2 = 2;
    private static final Integer N3 = 3;
    private static final Integer N4 = 4;

    private static final Float N1_N2 = 0.7f;
    private static final Float N1_N3 = 0.3f;
    private static final Float N2_N4 = 0.6f;
    private static final Float N2_N1 = 0.4f;
    private static final Float N3_N1 = 1.0f;
    private static final Float N4_N2 = 1.0f;

    /**
     * Gets a builder used to create the graph.
     *
     * @return
     */
    protected abstract <K> GraphBuilder<K> getGraphBuilder();

    /**
     * 1 -- w=0.7 --> 2 1 -- w=0.3 --> 3 2 -- w=0.6 --> 4 2 -- w=0.4 --> 1 3 --
     * w=1.0 --> 1 4 -- w=1.0 --> 2
     */
    private Graph<Integer> buildGraph() {
        GraphBuilder<Integer> builder = getGraphBuilder();

        addEdge(builder, N1, N3, DEFAULT_EDGE_TYPE, N1_N3)
            .addEdge(builder, N1, N2, DEFAULT_EDGE_TYPE, N1_N2)
            .addEdge(builder, N2, N1, DEFAULT_EDGE_TYPE, N2_N1)
            .addEdge(builder, N2, N4, DEFAULT_EDGE_TYPE, N2_N4)
            .addEdge(builder, N3, N1, DEFAULT_EDGE_TYPE, N3_N1)
            .addEdge(builder, N4, N2, DEFAULT_EDGE_TYPE, N4_N2);

        return builder.build();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleEdgeTypes() {
        GraphBuilder<Integer> builder = getGraphBuilder();

        addEdge(builder, N1, N3, DEFAULT_EDGE_TYPE, N1_N3)
            .addEdge(builder, N1, N2, SECONDARY_EDGE_TYPE, N1_N2)
            .addEdge(builder, N2, N1, DEFAULT_EDGE_TYPE, N2_N1)
            .addEdge(builder, N2, N4, DEFAULT_EDGE_TYPE, N2_N4)
            .addEdge(builder, N3, N1, SECONDARY_EDGE_TYPE, N3_N1)
            .addEdge(builder, N4, N2, SECONDARY_EDGE_TYPE, N4_N2);

        Graph<Integer> graph = builder.build();
        Traverser<Integer> traverser =
            graph.getTraverser(getNodeId(N1), SECONDARY_EDGE_TYPE);

        NodeID<Integer> node1 = getNodeId(N1);
        NodeID<Integer> node2 = getNodeId(N2);
        GraphEdge<Integer> edge =
            new GraphEdge<Integer>(node1, node2, SECONDARY_EDGE_TYPE, N1_N2);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(edge);
        testTraversal(traverser, expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEdgeFilter() {
        Graph<Integer> graph = buildGraph();
        EdgeFilter<Integer> filter = new EdgeFilter<Integer>() {

            @Override
            public boolean accepts(NodeID<Integer> from, NodeID<Integer> to) {
                return to.getID().intValue() > 2;
            }
        };

        Traverser<Integer> traverser =
            graph.getTraverser(getNodeId(N1), DEFAULT_EDGE_TYPE)
                .setReturnableFilter(filter);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N3, N1_N3));
        testTraversal(traverser, expected);
    }

    @Test
    public void testDenyAllFilter() {
        GraphBuilder<Integer> builder = getGraphBuilder();

        addEdge(builder, 1, 2, DEFAULT_EDGE_TYPE, 0.1f)
            .addEdge(builder, 1, 3, DEFAULT_EDGE_TYPE, 0.2f)
            .addEdge(builder, 2, 1, DEFAULT_EDGE_TYPE, 0.3f)
            .addEdge(builder, 2, 4, DEFAULT_EDGE_TYPE, 0.4f)
            .addEdge(builder, 3, 1, DEFAULT_EDGE_TYPE, 0.5f)
            .addEdge(builder, 3, 4, DEFAULT_EDGE_TYPE, 0.6f)
            .addEdge(builder, 3, 5, DEFAULT_EDGE_TYPE, 0.7f)
            .addEdge(builder, 4, 2, DEFAULT_EDGE_TYPE, 0.8f)
            .addEdge(builder, 4, 3, DEFAULT_EDGE_TYPE, 0.9f)
            .addEdge(builder, 4, 5, DEFAULT_EDGE_TYPE, 1.0f)
            .addEdge(builder, 5, 3, DEFAULT_EDGE_TYPE, 1.1f)
            .addEdge(builder, 5, 4, DEFAULT_EDGE_TYPE, 1.2f)
            .addEdge(builder, 5, 6, DEFAULT_EDGE_TYPE, 1.3f)
            .addEdge(builder, 6, 5, DEFAULT_EDGE_TYPE, 1.4f);

        Graph<Integer> graph = builder.build();
        EdgeFilter<Integer> filter = new EdgeFilter<Integer>() {

            @Override
            public boolean accepts(NodeID<Integer> from, NodeID<Integer> to) {
                return false;
            }
        };

        Traverser<Integer> traverser =
            graph.getTraverser(getNodeId(1), DEFAULT_EDGE_TYPE)
                .setReturnableFilter(filter).setMaxDepth(3);

        List<GraphEdge<Integer>> expected =
            Collections.emptyList();
        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleTraversal() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.getTraverser(getNodeId(N1), DEFAULT_EDGE_TYPE);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N2, N1_N2),
                          newEdge(N1, N3, N1_N3));

        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMaxReturnedEdges() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.getTraverser(getNodeId(N1), DEFAULT_EDGE_TYPE)
                .setMaxReturnedEdges(1);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N2, N1_N2));

        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMaxTraversedEdges() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.getTraverser(getNodeId(N1), DEFAULT_EDGE_TYPE)
                .setMaxTraversedEdges(1);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N2, N1_N2));

        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMaxDepth() {
        Graph<Integer> graph = buildGraph();

        Traverser<Integer> traverser =
            graph.getTraverser(getNodeId(N1), DEFAULT_EDGE_TYPE).setMaxDepth(2);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N2, N1_N2),
                          newEdge(N1, N3, N1_N3),
                          newEdge(N2, N4, N2_N4));

        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPeekNext() {
        Graph<Integer> graph = buildGraph();

        Traverser<Integer> traverser =
            graph.getTraverser(getNodeId(1), DEFAULT_EDGE_TYPE);

        List<GraphEdge<Integer>> edges = new ArrayList<GraphEdge<Integer>>();
        GraphCursor<Integer> cursor = null;
        try {
            cursor = traverser.traverse();
            while (cursor.hasNext()) {
                edges.add(cursor.peekNext());
                edges.add(cursor.next());
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N2, N1_N2), newEdge(N1, N2, N1_N2),
                          newEdge(N1, N3, N1_N3), newEdge(N1, N3, N1_N3));

        assertEquals(expected, edges);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMultiTraversal() {
        Graph<Integer> graph = buildGraph();

        // Basic test
        Traverser<Integer> traverser =
            graph.getMultiTraverser(Arrays.asList(getNodeId(1),
                                                  getNodeId(2)), DEFAULT_EDGE_TYPE);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N2, N1_N2),
                          newEdge(N2, N4, N2_N4),
                          newEdge(N2, N1, N2_N1),
                          newEdge(N1, N3, N1_N3));

        testTraversal(traverser, expected);

        // Test with limited return count
        traverser =
            graph.getMultiTraverser(Arrays.asList(getNodeId(1),
                                                  getNodeId(2)), DEFAULT_EDGE_TYPE)
                .setMaxReturnedEdges(3);

        expected =
            Arrays.asList(newEdge(N1, N2, N1_N2),
                          newEdge(N2, N4, N2_N4),
                          newEdge(N2, N1, N2_N1));

        testTraversal(traverser, expected);

        // Test with common end nodes
        traverser =
            graph.getMultiTraverser(Arrays.asList(getNodeId(1),
                                                  getNodeId(4)), DEFAULT_EDGE_TYPE);

        expected =
            Arrays.asList(newEdge(N4, N2, N4_N2),
                          newEdge(N1, N3, N1_N3));

        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCombination() {
        GraphBuilder<Integer> builder = getGraphBuilder();

        addEdge(builder, 1, 2, DEFAULT_EDGE_TYPE, 0.1f)
            .addEdge(builder, 1, 3, DEFAULT_EDGE_TYPE, 0.2f)
            .addEdge(builder, 2, 1, DEFAULT_EDGE_TYPE, 0.3f)
            .addEdge(builder, 2, 4, DEFAULT_EDGE_TYPE, 0.4f)
            .addEdge(builder, 3, 1, DEFAULT_EDGE_TYPE, 0.5f)
            .addEdge(builder, 3, 4, DEFAULT_EDGE_TYPE, 0.6f)
            .addEdge(builder, 3, 5, DEFAULT_EDGE_TYPE, 0.7f)
            .addEdge(builder, 4, 2, DEFAULT_EDGE_TYPE, 0.8f)
            .addEdge(builder, 4, 3, DEFAULT_EDGE_TYPE, 0.9f)
            .addEdge(builder, 4, 5, DEFAULT_EDGE_TYPE, 1.0f)
            .addEdge(builder, 5, 3, DEFAULT_EDGE_TYPE, 1.1f)
            .addEdge(builder, 5, 4, DEFAULT_EDGE_TYPE, 1.2f)
            .addEdge(builder, 5, 6, DEFAULT_EDGE_TYPE, 1.3f)
            .addEdge(builder, 6, 5, DEFAULT_EDGE_TYPE, 1.4f);

        EdgeFilter<Integer> filter = new EdgeFilter<Integer>() {
            @Override
            public boolean accepts(NodeID<Integer> start, NodeID<Integer> end) {
                return !start.getID().equals(2) && !end.getID().equals(2);
            }
        };

        Graph<Integer> graph = builder.build();

        Traverser<Integer> traverser =
            graph.getTraverser(getNodeId(1), DEFAULT_EDGE_TYPE).setMaxReturnedEdges(3)
                .setReturnableFilter(filter).setMaxDepth(3)
                .setMaxTraversedEdges(10);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(1, 3, 0.2f), newEdge(3, 5, 0.7f),
                          newEdge(3, 4, 0.6f));
        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleForEach() {
        Graph<Integer> graph = buildGraph();
        final List<NodeID<Integer>> neighbors = new ArrayList<NodeID<Integer>>();
        List<NodeID<Integer>> expected = Arrays.asList(getNodeId(N2), getNodeId(N3));
        NodeIDProcedure<Integer> proc = new NodeIDProcedure<Integer>() {

            @Override
            public boolean apply(NodeID<Integer> neighbor) {
                neighbors.add(neighbor);
                return true;
            }
        };
        graph.forEachNeighbor(getNodeId(N1), DEFAULT_EDGE_TYPE, proc);
        Assert.assertEquals("Wrong neighbors found in forEach loop.", expected, neighbors);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBreakForEach() {
        Graph<Integer> graph = buildGraph();
        final List<NodeID<Integer>> neighbors = new ArrayList<NodeID<Integer>>();
        List<NodeID<Integer>> expected = Arrays.asList(getNodeId(N2));
        NodeIDProcedure<Integer> proc = new NodeIDProcedure<Integer>() {

            @Override
            public boolean apply(NodeID<Integer> neighbor) {
                neighbors.add(neighbor);
                return false;
            }
        };
        graph.forEachNeighbor(getNodeId(N1), DEFAULT_EDGE_TYPE, proc);
        Assert.assertEquals("Wrong neighbors found in forEach loop.", expected, neighbors);
    }

    private static GraphEdge<Integer>
        newEdge(int n1,
                int n2,
                float weight) {
        NodeID<Integer> node1 = getNodeId(n1);
        NodeID<Integer> node2 = getNodeId(n2);
        return new GraphEdge<Integer>(node1, node2, DEFAULT_EDGE_TYPE, weight);
    }

    private void
        testTraversal(Traverser<Integer> traverser,
                      List<GraphEdge<Integer>> expected) {
        List<GraphEdge<Integer>> edges = new ArrayList<GraphEdge<Integer>>();
        GraphCursor<Integer> cursor = null;
        try {
            cursor = traverser.traverse();
            while (cursor.hasNext())
                edges.add(cursor.next());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        assertEquals(expected, edges);
        assertEquals(expected, traverser.getPath());
    }

    private static NodeID<Integer> getNodeId(int id) {
        return new NodeID<Integer>(id, NODE_TYPE);
    }

    private AbstractTestGraph addEdge(GraphBuilder<Integer> builder,
                                      int startNode,
                                      int endNode,
                                      EdgeType edgeType, float weight) {
        int startNodeIndex = builder.addOrGetNode(getNodeId(startNode));
        int endNodeIndex = builder.addOrGetNode(getNodeId(endNode));
        builder.addEdge(startNodeIndex, endNodeIndex, edgeType, weight);
        return this;
    }
}
