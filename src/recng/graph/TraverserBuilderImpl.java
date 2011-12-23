package recng.graph;

/**
 * A class used to build graph traversers.
 *
 * @author jon
 *
 * @param <T>
 *            The type of the node ids in the graph.
 */
class TraverserBuilderImpl<T> implements TraverserBuilder<T> {

    private final GraphNode<T> startNode;
    private final EdgeType edgeType;
    private EdgeFilter<T> returnableFilter =
        new EdgeFilter<T>() {
            public boolean accepts(NodeId<T> start, NodeId<T> end) {
                return true;
        }
    };
    private int maxDepth = 1;
    private int maxReturnedEdges = Integer.MAX_VALUE;
    private int maxTraversedEdges = Integer.MAX_VALUE;

    TraverserBuilderImpl(GraphNode<T> startNode, EdgeType edgeType) {
        this.startNode = startNode;
        this.edgeType = edgeType;
    }

    public synchronized TraverserBuilder<T> edgeFilter(EdgeFilter<T> returnableFilter) {
        this.returnableFilter = returnableFilter;
        return this;
    }

    public synchronized TraverserBuilder<T> maxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public synchronized TraverserBuilder<T> maxReturnedEdges(int maxReturnedEdges) {
        this.maxReturnedEdges = maxReturnedEdges;
        return this;
    }

    public synchronized TraverserBuilder<T> maxTraversedEdges(int maxTraversedEdges) {
        this.maxTraversedEdges = maxTraversedEdges;
        return this;
    }

    public synchronized Traverser<T> build() {
        return new TraverserImpl<T>(startNode, edgeType, returnableFilter,
                                    maxDepth, maxReturnedEdges,
                                    maxTraversedEdges);
    }
}
