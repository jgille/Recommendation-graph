package recng.graph;

import recng.common.Consumer;

/**
 * A graph containing nodes with (possibly weighted) edges to other nodes.
 *
 * @author Jon Ivmark
 */
public interface Graph<T> {

    /**
     * Returns an instance used to set up a graph traversal.
     */
    TraverserBuilder<T> prepareTraversal(NodeID<T> source,
                                         EdgeType eType);

    /**
     * Gets all nodes (sorted by ascending primary key) in the graph and passes
     * them to the consumer.
     *
     */
    void getAllNodes(Consumer<NodeID<T>, Void> consumer);

    /**
     * Gets all edges in the graph and passes them to the consumer.
     *
     */
    void getAllEdges(Consumer<GraphEdge<T>, Void> consumer);

    /**
     * Gets the number of nodes in this graph.
     */
    int nodeCount();

    /**
     * Gets the number of nodes in this graph.
     */
    int edgeCount();

    /**
     * Gets the primary key of a node, or -1 if no such node exists.
     */
    int getPrimaryKey(NodeID<T> nodeID);

    /**
     * Gets metadata about this graph.
     */
    GraphMetadata getMetadata();
}
