package rectest.graph;

import rectest.common.Consumer;

/**
 * A graph containing nodes with (possibly weighted) edges to other nodes.
 *
 * @author Jon Ivmark
 */
public interface Graph<K> {

    /**
     * Returns an instance used to set up a graph traversal.
     */
    TraverserBuilder<K> prepareTraversal(NodeId<K> source,
                                         EdgeType eType);

    /**
     * Gets all edges in the graph and passes them to the consumer.
     *
     */
    void getEdges(Consumer<GraphEdge<K>, Void> consumer);

    /**
     * Gets the number of nodes in this graph.
     */
    int nodeCount();

    /**
     * Gets the number of nodes in this graph.
     */
    int edgeCount();
}
