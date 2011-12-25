package tests.junit.graph;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import recng.graph.*;
import recng.recommendations.ProductNodeType;
import recng.recommendations.RecommendationType;

@SuppressWarnings("unchecked")
public abstract class AbstractTestGraph {

    private static final RecommendationType EDGE_TYPE = RecommendationType.PEOPLE_WHO_BOUGHT;

    private static final NodeType NODE_TYPE = ProductNodeType.getInstance();

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

    protected abstract <K> GraphBuilder<K> getGraphBuilder();

    /**
     * 1 -- w=0.7 --> 2
     * 1 -- w=0.3 --> 3
     * 2 -- w=0.6 --> 4
     * 2 -- w=0.4 --> 1
     * 3 -- w=1.0 --> 1
     * 4 -- w=1.0 --> 2
     */
    private Graph<Integer> buildGraph() {
        GraphBuilder<Integer> builder = getGraphBuilder();

        addEdge(builder, N1, N3, EDGE_TYPE, N1_N3)
            .addEdge(builder, N1, N2, EDGE_TYPE, N1_N2)
            .addEdge(builder, N2, N1, EDGE_TYPE, N2_N1)
            .addEdge(builder, N2, N4, EDGE_TYPE, N2_N4)
            .addEdge(builder, N3, N1, EDGE_TYPE, N3_N1)
            .addEdge(builder, N4, N2, EDGE_TYPE, N4_N2);

        return builder.build();
    }

    @Test
    public void testEdgeFilter() {
        Graph<Integer> graph = buildGraph();
        EdgeFilter<Integer> filter = new EdgeFilter<Integer>() {

            @Override
            public boolean accepts(NodeID<Integer> from, NodeID<Integer> to) {
                return to.getID().intValue() > 2;
            }
        };

        Traverser<Integer> traverser =
            graph.prepareTraversal(getNodeId(N1), EDGE_TYPE).edgeFilter(filter)
                .build();
        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N3, N1_N3));
        testTraversal(traverser, expected);
    }

    @Test
    public void testSimpleTraversal() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.prepareTraversal(getNodeId(N1), EDGE_TYPE).build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N2, N1_N2),
                          newEdge(N1, N3, N1_N3));

        testTraversal(traverser, expected);
    }

    @Test
    public void testMaxReturnedEdges() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.prepareTraversal(getNodeId(N1), EDGE_TYPE)
                .maxReturnedEdges(1).build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N2, N1_N2));

        testTraversal(traverser, expected);
    }

    @Test
    public void testMaxTraversedEdges() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.prepareTraversal(getNodeId(N1), EDGE_TYPE)
                .maxTraversedEdges(1).build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N2, N1_N2));

        testTraversal(traverser, expected);
    }

    @Test
    public void testMaxDepth() {
        Graph<Integer> graph = buildGraph();

        Traverser<Integer> traverser =
            graph.prepareTraversal(getNodeId(N1), EDGE_TYPE).maxDepth(2)
                .build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(N1, N2, N1_N2),
                          newEdge(N1, N3, N1_N3),
                          newEdge(N2, N4, N2_N4));

        testTraversal(traverser, expected);
    }


    @Test
    public void testCombination() {
        GraphBuilder<Integer> builder = getGraphBuilder();

        addEdge(builder, 1, 2, EDGE_TYPE, 0.1f)
            .addEdge(builder, 1, 3, EDGE_TYPE, 0.2f)
            .addEdge(builder, 2, 1, EDGE_TYPE, 0.3f)
            .addEdge(builder, 2, 4, EDGE_TYPE, 0.4f)
            .addEdge(builder, 3, 1, EDGE_TYPE, 0.5f)
            .addEdge(builder, 3, 4, EDGE_TYPE, 0.6f)
            .addEdge(builder, 3, 5, EDGE_TYPE, 0.7f)
            .addEdge(builder, 4, 2, EDGE_TYPE, 0.8f)
            .addEdge(builder, 4, 3, EDGE_TYPE, 0.9f)
            .addEdge(builder, 4, 5, EDGE_TYPE, 1.0f)
            .addEdge(builder, 5, 3, EDGE_TYPE, 1.1f)
            .addEdge(builder, 5, 4, EDGE_TYPE, 1.2f)
            .addEdge(builder, 5, 6, EDGE_TYPE, 1.3f)
            .addEdge(builder, 6, 5, EDGE_TYPE, 1.4f);

        EdgeFilter<Integer> filter = new EdgeFilter<Integer>() {
            @Override
            public boolean accepts(NodeID<Integer> start, NodeID<Integer> end) {
                return !start.getID().equals(2) && !end.getID().equals(2);
            }
        };

        Graph<Integer> graph = builder.build();

        Traverser<Integer> traverser =
            graph.prepareTraversal(getNodeId(1), EDGE_TYPE).maxReturnedEdges(3)
                .edgeFilter(filter).maxDepth(3).maxTraversedEdges(10).build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(1, 3, 0.2f), newEdge(3, 5, 0.7f),
                          newEdge(3, 4, 0.6f));
        testTraversal(traverser, expected);
    }

    private static GraphEdge<Integer>
        newEdge(int n1,
                int n2,
                float weight) {
        NodeID<Integer> node1 = getNodeId(n1);
        NodeID<Integer> node2 = getNodeId(n2);
        return new GraphEdge<Integer>(node1, node2, EDGE_TYPE, weight);
    }

    private
        void
        testTraversal(Traverser<Integer> traverser,
                      List<GraphEdge<Integer>> expected) {
        List<GraphEdge<Integer>> edges = new ArrayList<GraphEdge<Integer>>();
        GraphCursor<Integer> cursor = null;
        try {
            cursor  = traverser.traverse();
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
        int startNodeIndex = builder.addNode(getNodeId(startNode));
        int endNodeIndex = builder.addNode(getNodeId(endNode));
        builder.addEdge(startNodeIndex, endNodeIndex, edgeType, weight);
        return this;
    }
}
