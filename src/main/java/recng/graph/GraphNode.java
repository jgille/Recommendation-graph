package recng.graph;

import java.util.Iterator;

/**
 * A node in a graph.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the node IDs
 */
interface GraphNode<T> {
    /**
     * Returns an iterator of this node's out edges.
     */
    Iterator<TraversableGraphEdge<T>> traverseNeighbors(EdgeType edgeType);

    /**
     * Return the node id.
     */
    NodeID<T> getNodeId();

    /**
     * Gets the total number of edges originating from this node.
     */
    int getEdgeCount();

    /**
     * Gets the graph this node is a member of.
     * 
     */
    Graph<T> getGraph();
}
