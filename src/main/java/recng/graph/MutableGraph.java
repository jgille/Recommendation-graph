package recng.graph;

import java.util.List;

/**
 * A graph containing nodes with (possibly weighted) edges to other nodes.
 *
 * Edges can be added, updated or removed.
 *
 * @author Jon Ivmark
 */
public interface MutableGraph<T> extends Graph<T> {

    /**
     * Adds an edge. This method will create nodes if they do not already exist
     * in the graph.
     */
    void addEdge(NodeID<T> startNode, NodeID<T> endNode, EdgeType edgeType,
                 float weight);

    /**
     * Updates an edge.
     *
     * @return True if an edge was updated, false if no such edge was found.
     */
    boolean updateEdge(NodeID<T> startNode, NodeID<T> endNode,
                       EdgeType edgeType, float weight);

    /**
     * Removed an edge.
     *
     * @return True if an edge was removed, false if no such edge was found.
     */
    boolean
        removeEdge(NodeID<T> startNode, NodeID<T> endNode, EdgeType edgeType);

    /**
     * Sets the edges for a node, replacing any previous edges of this edge
     * type. This method will create nodes if they do not already exist in the
     * graph.
     */
    void setEdges(NodeID<T> startNode, EdgeType edgeType,
                  List<NodeID<T>> endNodes, List<Float> weights);
}
