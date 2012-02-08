package recng.graph;

import java.util.List;

import recng.common.Consumer;

/**
 * A graph containing nodes with (possibly weighted) edges to other nodes.
 *
 * @author Jon Ivmark
 */
public interface Graph<T> {

    /**
     * Returns a traverser used for graph traversal.
     */
    Traverser<T> getTraverser(NodeID<T> source, EdgeType eType);

    /**
     * Returns an instance used for a graph traversal originating from multiple
     * start nodes. Traversed paths are merged based on edge weight with the
     * heaviest edges first.
     */
    Traverser<T> getMultiTraverser(List<NodeID<T>> sourceNodes, EdgeType eType);

    /**
     * Gets all nodes in the graph and passes them to the consumer.
     *
     */
    void getAllNodes(Consumer<NodeID<T>, Void> consumer);

    /**
     * Gets nodes of a certain type in the graph and passes them to the
     * consumer.
     *
     */
    void getNodes(Consumer<NodeID<T>, Void> consumer, NodeType nodeType);

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
     * Gets a node by it's primary key.
     */
    GraphNode<T> getNode(int primaryKey);

    /**
     * Gets metadata about this graph.
     */
    GraphMetadata getMetadata();

    /**
     * Gets status info for this graph.
     */
    GraphStatus getStatus();
}
