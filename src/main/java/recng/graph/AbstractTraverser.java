package recng.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract traverser implementation.
 *
 * @author jon
 *
 * @param <T>
 */
abstract class AbstractTraverser<T> implements Traverser<T> {

    private final EdgeType edgeType;
    private EdgeFilter<T> returnableFilter =
        new EdgeFilter<T>() {
            public boolean accepts(NodeID<T> start, NodeID<T> end) {
                return true;
            }
        };
    private int maxDepth = 1;
    private int maxReturnedEdges = Integer.MAX_VALUE;
    private int maxTraversedEdges = Integer.MAX_VALUE;

    /**
     * Creates a traverser.
     *
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
    public AbstractTraverser(EdgeType edgeType) {
        this.edgeType = edgeType;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public EdgeFilter<T> getReturnableFilter() {
        return returnableFilter;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getMaxReturnedEdges() {
        return maxReturnedEdges;
    }

    public int getMaxTraversedEdges() {
        return maxTraversedEdges;
    }

    public Traverser<T> setReturnableFilter(EdgeFilter<T> returnableFilter) {
        this.returnableFilter = returnableFilter;
        return this;
    }

    public Traverser<T> setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public Traverser<T> setMaxReturnedEdges(int maxReturnedEdges) {
        this.maxReturnedEdges = maxReturnedEdges;
        return this;
    }

    public Traverser<T> setMaxTraversedEdges(int maxTraversedEdges) {
        this.maxTraversedEdges = maxTraversedEdges;
        return this;
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

    /**
     * Return the graph that is being traversed.
     */
    protected abstract Graph<T> getGraph();

    protected void logTraversalStats(long startTime, int returnedEdges,
                                     int traversedEdges) {
        long now = System.currentTimeMillis();
        long delta = now - startTime;
        GraphStatus status = getGraph().getStatus();
        status.incNumberOfTraversals();
        status.incRequestedEdges(getMaxReturnedEdges());
        status.incReturnedEdges(returnedEdges);
        status.incTraversedEdges(traversedEdges);
        status.incTraversalTime(delta);
    }

}
