package rectest.graph;

/**
 * Used to filter edges during graph traversal.
 *
 * @author jon
 *
 * @param <K>
 */
public interface EdgeFilter<K> {

    /**
     * Decides whether or not to accept an edge.
     */
    boolean accepts(NodeId<K> startNode, NodeId<K> endNode);

}
