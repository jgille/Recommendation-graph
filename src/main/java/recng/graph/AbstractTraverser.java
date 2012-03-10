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

    private final Graph<T> graph;
    private final EdgeType edgeType;
    private EdgeFilter<T> returnableFilter;
    private int maxDepth;
    private int maxReturnedEdges;
    private int maxTraversedEdges;

    /**
     * Creates a traverser.
     *
     * @param graph
     *            The graph instance that is being traversed.
     * @param edgeType
     *            The type of edges to follow
     */
    public AbstractTraverser(Graph<T> graph, EdgeType edgeType) {
        this.graph = graph;
        this.edgeType = edgeType;
        this.returnableFilter = new EdgeFilter<T>() {
            public boolean accepts(NodeID<T> start, NodeID<T> end) {
                return true;
            }
        };
        this.maxDepth = 1;
        this.maxReturnedEdges = Integer.MAX_VALUE;
        this.maxTraversedEdges = Integer.MAX_VALUE;
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
    protected Graph<T> getGraph() {
        return graph;
    }

    protected void logTraversalStats(long startTime, int returnedEdges,
                                     int traversedEdges) {
        GraphStats status = getGraph().getStats();
        status.incTraversals();
        status.incTraversedEdges(traversedEdges);
    }

}
