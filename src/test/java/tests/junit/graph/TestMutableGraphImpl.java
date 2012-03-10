package tests.junit.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import recng.graph.*;
import recng.recommendations.domain.RecommendationNodeType;
import recng.recommendations.graph.RecommendationGraphMetadata;
import recng.recommendations.graph.RecommendationEdgeType;

import static org.junit.Assert.*;

/**
 * Tests {@link MutableGraphImpl}.
 * 
 * @author jon
 * 
 */
public class TestMutableGraphImpl {

    private static final RecommendationEdgeType EDGE_TYPE =
        RecommendationEdgeType.PEOPLE_WHO_BOUGHT;

    private static final RecommendationEdgeType SECONDARY_EDGE_TYPE =
        RecommendationEdgeType.PEOPLE_WHO_VIEWED;

    private static final GraphMetadata GRAPH_METADATA =
        RecommendationGraphMetadata.getInstance();

    @SuppressWarnings("unchecked")
    private static final List<NodeID<Integer>> NODES =
        Arrays.<NodeID<Integer>> asList(createProductID(0),
                                        createProductID(1),
                                        createProductID(2),
                                        createProductID(3),
                                        createProductID(4),
                                        createProductID(5),
                                        createProductID(6),
                                        createProductID(7));

    private static NodeID<Integer> createProductID(int id) {
        return new NodeID<Integer>(id, RecommendationNodeType.PRODUCT);
    }

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
        graph.addEdge(NODES.get(3), NODES.get(5), SECONDARY_EDGE_TYPE, 3f / 5);
        graph.addEdge(NODES.get(3), NODES.get(4), SECONDARY_EDGE_TYPE, 3f / 4);
        graph.addEdge(NODES.get(4), NODES.get(2), SECONDARY_EDGE_TYPE, 4f / 2);

        return graph;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPeekNext() {
        Graph<Integer> graph = buildGraph();

        Traverser<Integer> traverser =
            graph.getTraverser(NODES.get(1), EDGE_TYPE);

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
            Arrays.asList(newEdge(NODES.get(1), NODES.get(2), 1f / 2),
                          newEdge(NODES.get(1), NODES.get(2), 1f / 2),
                          newEdge(NODES.get(1), NODES.get(3), 1f / 3),
                          newEdge(NODES.get(1), NODES.get(3), 1f / 3));

        assertEquals(expected, edges);
    }

    @Test
    public void testBuild() {
        MutableGraph<Integer> graph = buildGraph();
        assertNotNull(graph);
        assertEquals(7, graph.nodeCount());
        assertEquals(17, graph.edgeCount());
    }

    @SuppressWarnings("unchecked")
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
            graph.getTraverser(NODES.get(1), EDGE_TYPE)
                .setReturnableFilter(filter);
        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(1), NODES.get(3), 1f / 3));
        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleTraversal() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.getTraverser(NODES.get(3), EDGE_TYPE);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(3), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(3), NODES.get(4), 3f / 4),
                          newEdge(NODES.get(3), NODES.get(5), 3f / 5));

        testTraversal(traverser, expected);

        boolean exception = false;
        try {
            traverser =
                graph.getTraverser(null, EDGE_TYPE);
            traverser.traverse();
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
        exception = false;
        try {
            traverser =
                graph.getTraverser(NODES.get(1), null);
            traverser.traverse();
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMaxReturnedEdges() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.getTraverser(NODES.get(1), EDGE_TYPE).setMaxReturnedEdges(1);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(1), NODES.get(2), 1f / 2));

        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMaxTravesredEdges() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.getTraverser(NODES.get(1), EDGE_TYPE)
                .setMaxTraversedEdges(1);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(1), NODES.get(2), 1f / 2));

        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMaxDepth() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.getTraverser(NODES.get(1), EDGE_TYPE)
                .setMaxDepth(2).setMaxReturnedEdges(3);
        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(1), NODES.get(2), 1f / 2),
                          newEdge(NODES.get(1), NODES.get(3), 1f / 3),
                          newEdge(NODES.get(2), NODES.get(4), 2f / 4));

        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
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
            graph.getTraverser(NODES.get(1), EDGE_TYPE)
                .setReturnableFilter(filter)
                .setMaxDepth(2).setMaxReturnedEdges(5).setMaxTraversedEdges(20);
        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(1), NODES.get(3), 1f / 3),
                          newEdge(NODES.get(3), NODES.get(4), 3f / 4),
                          newEdge(NODES.get(3), NODES.get(5), 3f / 5));

        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMultiTraversal() {
        Graph<Integer> graph = buildGraph();
        Traverser<Integer> traverser =
            graph.getMultiTraverser(Arrays.asList(NODES.get(3),
                                                  NODES.get(4)), EDGE_TYPE);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(3), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(4), NODES.get(2), 4f / 2),
                          newEdge(NODES.get(4), NODES.get(3), 4f / 3),
                          newEdge(NODES.get(4), NODES.get(5), 4f / 5),
                          newEdge(NODES.get(3), NODES.get(4), 3f / 4));

        testTraversal(traverser, expected);

        traverser =
            graph.getMultiTraverser(Arrays.asList(NODES.get(3),
                                                  NODES.get(4)), EDGE_TYPE)
                .setMaxReturnedEdges(4);

        expected =
            Arrays.asList(newEdge(NODES.get(3), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(4), NODES.get(2), 4f / 2),
                          newEdge(NODES.get(4), NODES.get(3), 4f / 3),
                          newEdge(NODES.get(4), NODES.get(5), 4f / 5));

        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddEdge() {
        MutableGraph<Integer> graph = buildGraph();
        graph.addEdge(NODES.get(3), NODES.get(2), EDGE_TYPE, 3f / 2);
        Traverser<Integer> traverser =
            graph.getTraverser(NODES.get(3), EDGE_TYPE);

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

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateEdge() {
        MutableGraph<Integer> graph = buildGraph();
        graph.addEdge(NODES.get(3), NODES.get(2), EDGE_TYPE, 3f / 2);
        assertTrue(graph.updateEdge(NODES.get(3), NODES.get(4), EDGE_TYPE,
                                    3f / 8));
        assertFalse(graph.updateEdge(NODES.get(6), NODES.get(1), EDGE_TYPE,
                                     3f / 8));

        Traverser<Integer> traverser =
            graph.getTraverser(NODES.get(3), EDGE_TYPE);

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

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveEdge() {
        MutableGraph<Integer> graph = buildGraph();
        assertTrue(graph.removeEdge(NODES.get(3), NODES.get(4), EDGE_TYPE));
        assertFalse(graph.removeEdge(NODES.get(6), NODES.get(1), EDGE_TYPE));
        Traverser<Integer> traverser =
            graph.getTraverser(NODES.get(3), EDGE_TYPE);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(3), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(3), NODES.get(5), 3f / 5));
        testTraversal(traverser, expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetEdges() {
        MutableGraph<Integer> graph = buildGraph();
        graph.setEdges(NODES.get(3), EDGE_TYPE,
                       Arrays.asList(NODES.get(2), NODES.get(1)),
                       Arrays.asList(3f / 2, 3f / 1));
        Traverser<Integer> traverser =
            graph.getTraverser(NODES.get(3), EDGE_TYPE);

        List<GraphEdge<Integer>> expected =
            Arrays.asList(newEdge(NODES.get(3), NODES.get(1), 3f / 1),
                          newEdge(NODES.get(3), NODES.get(2), 3f / 2));
        testTraversal(traverser, expected);

        graph.setEdges(NODES.get(7), EDGE_TYPE,
                       Arrays.asList(NODES.get(2), NODES.get(1)),
                       Arrays.asList(3f / 2, 3f / 1));
        traverser =
            graph.getTraverser(NODES.get(7), EDGE_TYPE);
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

    private void
        testTraversal(Traverser<Integer> traverser,
                      List<GraphEdge<Integer>> expected) {
        List<GraphEdge<Integer>> edges =
            new ArrayList<GraphEdge<Integer>>();
        GraphCursor<Integer> cursor = null;
        try {
            cursor = traverser.traverse();
            while (cursor.hasNext()) {
                GraphEdge<Integer> next = cursor.next();
                edges.add(next);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        assertEquals(expected, edges);
        assertEquals(expected, traverser.getPath());
    }

}
