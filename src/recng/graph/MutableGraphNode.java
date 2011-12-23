package recng.graph;

import java.util.List;

/**
 * A mutable graph node. The identifiers of the nodes are {@link NodeID}
 * instances, and the primary keys used internally in the graph are integers.
 *
 * @author jon
 *
 * @param <T>
 *            The type of the key for this node.
 */
public interface MutableGraphNode<T> extends GraphNode<T> {

    /**
     * Gets the total number of edges originating from this node.
     */
    int getEdgeCount();

    /**
     * Adds a new edge originating from this node.
     *
     * @param endNodeIndex
     *            The primary key of the neighbor node
     * @param edgeType
     *            The type of edge to create
     * @param weight
     *            The weight of the created edge.
     */
    void addEdge(int endNodeIndex, EdgeType edgeType, float weight);

    /**
     *
     * @param endNodeIndex
     *            The primary key of the neighbor node
     * @param edgeType
     *            The type of the edge to update
     * @param weight
     *            The new edge weight
     * @return True if an edge was updated, false if no matching edge was found.
     */
    boolean updateEdge(int endNodeIndex, EdgeType edgeType, float weight);

    /**
     *
     * @param endNodeIndex
     *            The primary key of the neighbor node
     * @param edgeType
     *            The type of the edge to remove
     * @return True if an edge was removed, false if no matching edge was found.
     */
    boolean removeEdge(int endNodeIndex, EdgeType edgeType);

    /**
     * Sets the set of edges of a certain type originating from ths node.
     *
     * @param edgeType
     *            The type of the edges to set
     * @param endNodes
     *            The primary keys of the edge end nodes
     * @param weights
     *            The edge weights
     */
    void setEdges(EdgeType edgeType,
                  List<Integer> endNodes,
                  List<Float> weights);

    /**
     * Returns a string describing this node in a more verbose way (for instance
     * including edges) than the standard toString method.
     */
    String toVerboseString();
}
