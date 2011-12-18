package recng.graph;

import java.util.ArrayList;
import java.util.List;

class TraverserImpl<K> implements Traverser<K> {

    private final GraphNode<K> startNode;
    private final EdgeType edgeType;
    private final EdgeFilter<K> returnableFilter;
    private final int maxDepth;
    private final int maxReturnedEdges;
    private final int maxTraversedEdges;

    TraverserImpl(GraphNode<K> startNode, EdgeType edgeType,
                  EdgeFilter<K> returnableFilter, int maxDepth,
                  int maxReturnedEdges, int maxTraversedEdges) {
        this.startNode = startNode;
        this.edgeType = edgeType;
        this.returnableFilter = returnableFilter;
        this.maxDepth = maxDepth;
        this.maxReturnedEdges = maxReturnedEdges;
        this.maxTraversedEdges = maxTraversedEdges;
    }

    public GraphCursor<K> traverse() {
        GraphIterator<K> iterator =
            new GraphIterator<K>(startNode, edgeType, returnableFilter,
                                 maxDepth, maxReturnedEdges, maxTraversedEdges);
        return new GraphCursorImpl<K>(iterator);
    }

    public List<GraphEdge<K>> getPath() {
        GraphCursor<K> cursor = null;
        List<GraphEdge<K>> res = new ArrayList<GraphEdge<K>>();
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
