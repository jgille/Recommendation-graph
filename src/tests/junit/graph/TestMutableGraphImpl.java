package tests.junit.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import recng.graph.*;
import recng.recommendations.ProductID;
import recng.recommendations.RecommendationGraphMetadata;
import recng.recommendations.RecommendationType;

import static org.junit.Assert.*;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class TestMutableGraphImpl {

    private static final RecommendationType EDGE_TYPE =
        RecommendationType.PEOPLE_WHO_BOUGHT;

    private static final GraphMetadata GRAPH_METADATA =
        RecommendationGraphMetadata.getInstance();

    private static final List<NodeID<Integer>> NODES =
        Arrays.<NodeID<Integer>> asList(new ProductID<Integer>(0),
                                        new ProductID<Integer>(1),
                                        new ProductID<Integer>(2),
                                        new ProductID<Integer>(3),
                                        new ProductID<Integer>(4),
                                        new ProductID<Integer>(5),
                                        new ProductID<Integer>(6),
                                        new ProductID<Integer>(7));

    private MutableGraph<Integer> buildGraph() {
        MutableGraph<Integer> graph =
            new MutableGraphImpl<Integer>(GRAPH_METADATA);
        graph.addEdge(NODES.get(0), NODES.get(1), EDGE_TYPE, 0f / 1);
        graph.addEdge(NODES.get(1), NODES.get(3), EDGE_TYPE, 1f / 3);
        graph.addEdge(NODES.get(1), NODES.get(2), EDGE_TYPE, 1f / 2);
        graph.addEdge(NODES.get(2), NODES.get(1), EDGE_TYPE, 2f / 1);
        graph.addEdge(NODES.get(2), NODES.get(4), EDGE_TYPE, 2f / 4);
        graph.addEdge(NODES.get(3), NODES.get(1), EDGE_TYPE, 3f / 1);
        graph.addEdge(NODES.get(3), NODES.get(5), EDGE_TYPE, 3f / 5);
        graph.addEdge(NODES.get(3), NODES.get(4), EDGE_TYPE, 3f / 4);
        graph.addEdge(NODES.get(4), NODES.get(2), EDGE_TYPE, 4f / 2);
        graph.addEdge(NODES.get(4), NODES.get(5), EDGE_TYPE, 4f / 5);
        graph.addEdge(NODES.get(4), NODES.get(3), EDGE_TYPE, 4f / 3);
        graph.addEdge(NODES.get(5), NODES.get(3), EDGE_TYPE, 5f / 3);
        graph.addEdge(NODES.get(5), NODES.get(6), EDGE_TYPE, 5f / 6);
        graph.addEdge(NODES.get(6), NODES.get(5), EDGE_TYPE, 6f / 5);
        return graph;
    }

    @Test
    public void testBuild() {
        MutableGraph<Integer> graph = buildGraph();
        assertNotNull(graph);
        assertEquals(7, graph.nodeCount());
        assertEquals(14, graph.edgeCount());
    }

    @Test
    public void testEdgeFilter() {
        MutableGraph<Integer> graph = buildGraph();
        EdgeFilter<Integer> filter = new EdgeFilter<Integer>() {

            @Override
            public boolean accepts(NodeID<Integer> start, NodeID<Integer> end) {
                return end.getID().intValue() > 2;
            }
        };

        Traverser<Integer> traverser =
            graph.prepareTraversal(NODES.get(1), EDGE_TYPE).edgeFilter(filter)
                .build();
        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(1), NODES.get(3), 1f / 3));
        testTraversal(traverser, expected);
    }

    @Test
    public void testSimpleTraversal() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.prepareTraversal(NODES.get(3), EDGE_TYPE).build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(3), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(3), NODES.get(4), 3f / 4),
                          newEdge(NODES.get(3), NODES.get(5), 3f / 5));

        testTraversal(traverser, expected);

        boolean exception = false;
        try {
            traverser =
                graph.prepareTraversal(null, EDGE_TYPE).build();
            traverser.traverse();
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
        exception = false;
        try {
            traverser =
                graph.prepareTraversal(NODES.get(1), null).build();
            traverser.traverse();
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testMaxReturnedEdges() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.prepareTraversal(NODES.get(1), EDGE_TYPE).maxReturnedEdges(1)
                .build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(1), NODES.get(2), 1f / 2));

        testTraversal(traverser, expected);
    }

    @Test
    public void testMaxTravesredEdges() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.prepareTraversal(NODES.get(1), EDGE_TYPE)
                .maxTraversedEdges(1).build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(1), NODES.get(2), 1f / 2));

        testTraversal(traverser, expected);
    }

    @Test
    public void testMaxDepth() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.prepareTraversal(NODES.get(1), EDGE_TYPE)
                .maxDepth(2).maxReturnedEdges(3).build();
        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(1), NODES.get(2), 1f / 2),
                          newEdge(NODES.get(1), NODES.get(3), 1f / 3),
                          newEdge(NODES.get(2), NODES.get(4), 2f / 4));

        testTraversal(traverser, expected);
    }

    @Test
    public void testCombination() {
        Graph<Integer> graph = buildGraph();
        EdgeFilter<Integer> filter = new EdgeFilter<Integer>() {
            @Override
            public boolean accepts(NodeID<Integer> start, NodeID<Integer> end) {
                return !start.getID().equals(2) && !end.getID().equals(2);
            }
        };

        Traverser<Integer> traverser =
            graph.prepareTraversal(NODES.get(1), EDGE_TYPE).edgeFilter(filter)
                .maxDepth(2).maxReturnedEdges(5).maxTraversedEdges(20).build();
        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(1), NODES.get(3), 1f / 3),
                          newEdge(NODES.get(3), NODES.get(4), 3f / 4),
                          newEdge(NODES.get(3), NODES.get(5), 3f / 5));

        testTraversal(traverser, expected);
    }

    @Test
    public void testAddEdge() {
        MutableGraph<Integer> graph = buildGraph();
        graph.addEdge(NODES.get(3), NODES.get(2), EDGE_TYPE, 3f / 2);
        Traverser<Integer> traverser =
            graph.prepareTraversal(NODES.get(3), EDGE_TYPE).build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(3), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(3), NODES.get(2), 3f / 2),
                          newEdge(NODES.get(3), NODES.get(4), 3f / 4),
                          newEdge(NODES.get(3), NODES.get(5), 3f / 5));
        testTraversal(traverser, expected);

        boolean exception = false;
        try {
            graph.addEdge(null, NODES.get(2), EDGE_TYPE, 3f / 2);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            graph.addEdge(NODES.get(2), null, EDGE_TYPE, 3f / 2);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            graph.addEdge(NODES.get(2), NODES.get(3), null, 3f / 2);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testUpdateEdge() {
        MutableGraph<Integer> graph = buildGraph();
        graph.addEdge(NODES.get(3), NODES.get(2), EDGE_TYPE, 3f / 2);
        assertTrue(graph.updateEdge(NODES.get(3), NODES.get(4), EDGE_TYPE,
                                    3f / 8));
        assertFalse(graph.updateEdge(NODES.get(6), NODES.get(1), EDGE_TYPE,
                                     3f / 8));

        Traverser<Integer> traverser =
            graph.prepareTraversal(NODES.get(3), EDGE_TYPE).build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(3), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(3), NODES.get(2), 3f / 2),
                          newEdge(NODES.get(3), NODES.get(5), 3f / 5),
                          newEdge(NODES.get(3), NODES.get(4), 3f / 8));
        testTraversal(traverser, expected);

        boolean exception = false;
        try {
            graph.updateEdge(null, NODES.get(2), EDGE_TYPE, 3f / 2);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            graph.updateEdge(NODES.get(2), null, EDGE_TYPE, 3f / 2);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            graph.updateEdge(NODES.get(2), NODES.get(3), null, 3f / 2);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testRemoveEdge() {
        MutableGraph<Integer> graph = buildGraph();
        assertTrue(graph.removeEdge(NODES.get(3), NODES.get(4), EDGE_TYPE));
        assertFalse(graph.removeEdge(NODES.get(6), NODES.get(1), EDGE_TYPE));
        Traverser<Integer> traverser =
            graph.prepareTraversal(NODES.get(3), EDGE_TYPE).build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(3), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(3), NODES.get(5), 3f / 5));
        testTraversal(traverser, expected);
    }

    @Test
    public void testSetEdges() {
        MutableGraph<Integer> graph = buildGraph();
        graph.setEdges(NODES.get(3), EDGE_TYPE,
                       Arrays.asList(NODES.get(2), NODES.get(1)),
                       Arrays.asList(3f / 2, 3f / 1));
        Traverser<Integer> traverser =
            graph.prepareTraversal(NODES.get(3), EDGE_TYPE).build();

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(3), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(3), NODES.get(2), 3f / 2));
        testTraversal(traverser, expected);

        graph.setEdges(NODES.get(7), EDGE_TYPE,
                       Arrays.asList(NODES.get(2), NODES.get(1)),
                       Arrays.asList(3f / 2, 3f / 1));
        traverser =
            graph.prepareTraversal(NODES.get(7), EDGE_TYPE).build();
        expected =
            Arrays.asList(newEdge(NODES.get(7), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(7), NODES.get(2), 3f / 2));
        testTraversal(traverser, expected);

        boolean exception = false;
        try {
            graph.setEdges(NODES.get(3), EDGE_TYPE,
                           Arrays.asList(NODES.get(2)),
                           Arrays.asList(3f / 2, 3f / 1));
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
        exception = false;
        try {
            graph.setEdges(NODES.get(3), EDGE_TYPE,
                           Arrays.asList(NODES.get(2), NODES.get(3)),
                           Arrays.asList(3f / 2));
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
        exception = false;
        try {
            graph.setEdges(NODES.get(3), EDGE_TYPE,
                           null,
                           Arrays.asList(3f / 2));
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }

    private static GraphEdge<Integer> newEdge(NodeID<Integer> n1,
                                              NodeID<Integer> n2,
                float weight) {
        return new GraphEdge<Integer>(n1, n2, EDGE_TYPE, weight);
    }

    private
        void
        testTraversal(Traverser<Integer> traverser,
                      List<GraphEdge<Integer>> expected) {
        List<GraphEdge<Integer>> edges =
            new ArrayList<GraphEdge<Integer>>();
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

}
