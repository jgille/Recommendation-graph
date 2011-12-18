package recng.graph;

class TraverserBuilderImpl<K> implements TraverserBuilder<K> {

    private final GraphNode<K> startNode;
    private final EdgeType edgeType;
    private EdgeFilter<K> returnableFilter =
        new EdgeFilter<K>() {
            public boolean accepts(NodeId<K> start, NodeId<K> end) {
            return true;
        }
    };
    private int maxDepth = 1;
    private int maxReturnedEdges = 20;
    private int maxTraversedEdges = 500;

    TraverserBuilderImpl(GraphNode<K> startNode, EdgeType edgeType) {
        this.startNode = startNode;
        this.edgeType = edgeType;
    }

    public synchronized TraverserBuilder<K> edgeFilter(EdgeFilter<K> returnableFilter) {
        this.returnableFilter = returnableFilter;
        return this;
    }

    public synchronized TraverserBuilder<K> maxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public synchronized TraverserBuilder<K> maxReturnedEdges(int maxReturnedEdges) {
        this.maxReturnedEdges = maxReturnedEdges;
        return this;
    }

    public synchronized TraverserBuilder<K> maxTraversedEdges(int maxTraversedEdges) {
        this.maxTraversedEdges = maxTraversedEdges;
        return this;
    }

    public synchronized Traverser<K> build() {
        return new TraverserImpl<K>(startNode, edgeType, returnableFilter,
                                    maxDepth, maxReturnedEdges,
                                    maxTraversedEdges);
    }
}
