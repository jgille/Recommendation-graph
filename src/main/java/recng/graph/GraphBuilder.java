package recng.graph;

/**
 * Used to build graphs.
 * 
 * @author jon
 * 
 * @param <T>
 *            The generic type of the node IDs in the graph.
 */
public interface GraphBuilder<T> {

    /**
     * Adds a node to the graph, if it is not already present in which case this
     * method does nothing.
     * 
     * @return The index of the added node.
     */
    int addOrGetNode(NodeID<T> node);

    /**
     * Returns the index for a node, or -1 if the node does not exist.
     */
    int getNodeIndex(NodeID<T> node);

    /**
     * Adds a weighted edge to the graph.
     * 
     * @param startNodeIndex
     *            The index of the start node.
     * @param endNodeIndex
     *            The index of the end node.
     * @param edgeType
     *            The edge type.
     * @param weight
     *            The edge weight.
     */
    void addEdge(int startNodeIndex, int endNodeIndex, EdgeType edgeType,
                 float weight);

    /**
     * Builds the graph.
     */
    Graph<T> build();
}
