package recng.graph;

/**
 * A class used to build graph traversers.
 * 
 * @author jon
 *
 * @param <K>
 *            The type of the node ids in the graph.
 */
public interface TraverserBuilder<K> {

    /**
     * Specifies a filter used to decide if a traversed edge should be
     * included in the result.
     */
    TraverserBuilder<K> edgeFilter(EdgeFilter<K> returnableFilter);

    /**
     * Specifies the maximum depth of the traversal.
     */
    TraverserBuilder<K> maxDepth(int maxDepth);

    /**
     * Specifies the maximum number of edges that may be returned.
     */
    TraverserBuilder<K> maxReturnedEdges(int maxReturnedEdges);

    /**
     * Specifies the maximum number of edges that may be traversed.
     */
    TraverserBuilder<K> maxTraversedEdges(int maxTraversedEdges);

    /**
     * Builds the traverser.
     */
    Traverser<K> build();
}