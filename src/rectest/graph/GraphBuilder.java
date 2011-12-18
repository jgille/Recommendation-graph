package rectest.graph;

/**
 * Used to build graphs.
 *
 * @author jon
 *
 * @param <K>
 */
public interface GraphBuilder<K> {

    GraphBuilder<K> addEdge(NodeId<K> from, NodeId<K> to, EdgeType edgeType,
                            float weight);

    Graph<K> build();

}
