package recng.graph;

import java.util.List;

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
     * Iterates all nodes in the graph and passes them to the procedure.
     *
     */
    void forEachNode(NodeIDProcedure<T> proc);

    /**
     * Iterates nodes of a certain type in the graph and passes them to the
     * procedure.
     *
     * Iteration will cease once the procedure call returns false.
     */
    void forEachNode(NodeIDProcedure<T> proc, NodeType nodeType);

    /**
     * Iterates all edges in the graph and passes them to the procedure.
     *
     * Iteration will cease once the procedure call returns false.
     */
    void forEachEdge(GraphEdgeProcedure<T> proc);

    /**
     * Iterates all neighbors for a node, following a certain edge type, and
     * passes them to the procedure.
     *
     * Iteration will cease once the procedure call returns false.
     */
    void forEachNeighbor(NodeID<T> source, EdgeType eType, NodeIDProcedure<T> proc);

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
     * Gets stats for this graph.
     */
    GraphStats getStats();
}
