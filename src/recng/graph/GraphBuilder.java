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

    GraphBuilder<T> addEdge(NodeID<T> from, NodeID<T> to, EdgeType edgeType,
                            float weight);

    Graph<T> build();

}
