package recng.graph;

/**
 * Classes used to build graph traversers should implement this interface.
 *
 * @author jon
 * 
 * @param <T>
 *            The type of the node ids in the graph.
 */
public interface TraverserBuilder<T> {

    /**
     * Specifies a filter used to decide if a traversed edge should be
     * included in the result.
     */
    TraverserBuilder<T> edgeFilter(EdgeFilter<T> returnableFilter);

    /**
     * Specifies the maximum depth of the traversal.
     */
    TraverserBuilder<T> maxDepth(int maxDepth);

    /**
     * Specifies the maximum number of edges that may be returned.
     */
    TraverserBuilder<T> maxReturnedEdges(int maxReturnedEdges);

    /**
     * Specifies the maximum number of edges that may be traversed.
     */
    TraverserBuilder<T> maxTraversedEdges(int maxTraversedEdges);

    /**
     * Builds the traverser.
     */
    Traverser<T> build();
}