package recng.graph;

/**
 * A traverser implementation.
 *
 * @author jon
 *
 * @param <T>
 */
class TraverserImpl<T> extends AbstractTraverser<T> implements Traverser<T> {

    private final GraphNode<T> startNode;

    /**
     * Creates a traverser.
     *
     * @param graph
     *            The graph to traverse.
     * @param startNode
     *            The node to start traversing from
     * @param edgeType
     *            The type of edges to follow
     */
    TraverserImpl(Graph<T> graph, GraphNode<T> startNode, EdgeType edgeType) {
        super(graph, edgeType);
        this.startNode = startNode;
    }

    public GraphCursor<T> traverse() {
        final long startTime = System.currentTimeMillis();
        GraphIterator<T> iterator =
            new GraphIterator<T>(startNode, getEdgeType(),
                                 getReturnableFilter(),
                                 getMaxDepth(), getMaxReturnedEdges(),
                                 getMaxTraversedEdges());
        return new GraphCursorImpl<T>(iterator) {
            @Override
            public void close() {
                super.close();
                logTraversalStats(startTime, getReturnedEdgeCount(),
                                  getTraversedEdgeCount());
            }
        };
    }

    @Override
    protected Graph<T> getGraph() {
        return startNode.getGraph();
    }
}
