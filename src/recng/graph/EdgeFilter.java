package recng.graph;

/**
 * Used to filter edges during graph traversal.
 *
 * @author jon
 *
 * @param <T>
 */
public interface EdgeFilter<T> {

    /**
     * Decides whether or not to accept an edge.
     */
    boolean accepts(NodeId<T> startNode, NodeId<T> endNode);

}
