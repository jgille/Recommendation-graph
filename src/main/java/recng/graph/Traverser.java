package recng.graph;

import java.util.List;

/**
 * A traverser used to traverse edges between product nodes in a graph.
 * 
 * NOTE: The effect of calling the setter method after a traversal is started is
 * undefined. All setters should be called before the traversal is commenced.
 *
 * NOTE: Implementations of this class can be assumed to be non thread safe.
 *
 * @author Jon Ivmark
 */
public interface Traverser<T> {

    /**
     * Gets and iterator used to traverse product nodes connected to a product,
     * according to the rules setup in the traverser.
     *
     * Neighbors are traversed ordered by descending edge weight.
     *
     * NOTE: Make sure you always close the cursor once you've started to
     * traverse it, i.e. close it in a finally block.
     */
    GraphCursor<T> traverse();

    /**
     * Gets the entire traversal path according to the rules setup in the
     * traverser.
     *
     * Neighbors are traversed ordered by descending edge weight.
     */
    List<GraphEdge<T>> getPath();

    /**
     * Specifies a filter used to decide if a traversed edge should be included
     * in the result.
     */
    Traverser<T> setReturnableFilter(EdgeFilter<T> returnableFilter);

    /**
     * Specifies the maximum depth of the traversal.
     */
    Traverser<T> setMaxDepth(int maxDepth);

    /**
     * Specifies the maximum number of edges that may be returned.
     */
    Traverser<T> setMaxReturnedEdges(int maxReturnedEdges);

    /**
     * Specifies the maximum number of edges that may be traversed.
     */
    Traverser<T> setMaxTraversedEdges(int maxTraversedEdges);
}
