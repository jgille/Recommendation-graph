package recng.graph;

import java.util.List;

/**
 * A graph containing nodes with (possibly weighted) edges to other nodes.
 *
 * Edges can be added, updated or removed.
 *
 * @author Jon Ivmark
 */
public interface MutableGraph<T> extends Graph<T>, MutableGraphNodeStore<T> {

    /**
     * Adds an edge. This method will create nodes if they do not already exist
     * in the graph.
     */
    void addEdge(NodeId<T> startNode, NodeId<T> endNode, EdgeType edgeType,
                 float weight);

    /**
     * Updates an edge.
     *
     * @return True if an edge was updated, false if no such edge was found.
     */
    boolean updateEdge(NodeId<T> startNode, NodeId<T> endNode,
                       EdgeType edgeType, float weight);

    /**
     * Removed an edge.
     *
     * @return True if an edge was removed, false if no such edge was found.
     */
    boolean
        removeEdge(NodeId<T> startNode, NodeId<T> endNode, EdgeType edgeType);

    /**
     * Sets the edges for a node, replacing any previous edges of this edge
     * type. This method will create nodes if they do not already exist in the
     * graph.
     */
    void setEdges(NodeId<T> startNode, EdgeType edgeType,
                  List<NodeId<T>> endNodes, List<Float> weights);
}
