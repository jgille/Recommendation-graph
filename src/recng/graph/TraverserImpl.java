package recng.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * A traverser implementation.
 *
 * @author jon
 *
 * @param <T>
 */
class TraverserImpl<T> implements Traverser<T> {

    private final GraphNode<T> startNode;
    private final EdgeType edgeType;
    private final EdgeFilter<T> returnableFilter;
    private final int maxDepth;
    private final int maxReturnedEdges;
    private final int maxTraversedEdges;

    /**
     * Creates a traverser.
     *
     * @param startNode
     *            The node to start traversing from
     * @param edgeType
     *            The type of edges to follow
     * @param returnableFilter
     *            A filter used to decide if a traversed edge should be included
     *            in the result.
     * @param maxDepth
     *            The maximum depth of the traversal
     * @param maxReturnedEdges
     *            The maximum number of edges that may be returned
     * @param maxTraversedEdges
     *            The maximum number of edges that may be traversed before
     *            returning
     */
    TraverserImpl(GraphNode<T> startNode, EdgeType edgeType,
                  EdgeFilter<T> returnableFilter, int maxDepth,
                  int maxReturnedEdges, int maxTraversedEdges) {
        this.startNode = startNode;
        this.edgeType = edgeType;
        this.returnableFilter = returnableFilter;
        this.maxDepth = maxDepth;
        this.maxReturnedEdges = maxReturnedEdges;
        this.maxTraversedEdges = maxTraversedEdges;
    }

    public GraphCursor<T> traverse() {
        GraphIterator<T> iterator =
            new GraphIterator<T>(startNode, edgeType, returnableFilter,
                                 maxDepth, maxReturnedEdges, maxTraversedEdges);
        return new GraphCursorImpl<T>(iterator);
    }

    public List<GraphEdge<T>> getPath() {
        GraphCursor<T> cursor = null;
        List<GraphEdge<T>> res = new ArrayList<GraphEdge<T>>();
        try {
            cursor = traverse();
            while (cursor.hasNext())
                res.add(cursor.next());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return res;
    }
}
