package recng.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A traverser implementation with multiple start nodes.
 *
 * @author jon
 *
 * @param <T>
 */
class MultiTraverser<T> extends AbstractTraverser<T> implements Traverser<T> {

    private final List<GraphNode<T>> startNodes;

    /**
     * Creates a traverser.
     *
     * @param startNodes
     *            The nodes to start traversing from
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
     *            The maximum number of traversed edges per start node.
     */
    MultiTraverser(List<GraphNode<T>> startNodes, EdgeType edgeType) {
        super(edgeType);
        this.startNodes = startNodes;
    }

    public GraphCursor<T> traverse() {
        List<GraphCursor<T>> cursors = new ArrayList<GraphCursor<T>>();
        final long startTime = System.currentTimeMillis();
        for (GraphNode<T> startNode : startNodes)
            cursors.add(getCursor(startNode));
        return new MultiCursor<T>(cursors, getMaxReturnedEdges()) {
            @Override
            public void close() {
                super.close();
                logTraversalStats(startTime, getReturnedEdgeCount(),
                                  getTraversedEdgeCount());
            }
        };
    }

    private GraphCursor<T> getCursor(GraphNode<T> startNode) {
        GraphIterator<T> iterator =
            new GraphIterator<T>(startNode, getEdgeType(),
                                 getReturnableFilter(),
                                 getMaxDepth(), getMaxReturnedEdges(),
                                 getMaxTraversedEdges());
        return new GraphCursorImpl<T>(iterator);

    }

    private static class MultiCursor<T> implements GraphCursor<T> {

        private final List<GraphCursor<T>> cursors;
        private final int limit;
        private final Set<NodeID<T>> returnedNodes;

        public MultiCursor(List<GraphCursor<T>> cursors, int limit) {
            this.cursors = cursors;
            this.limit = limit;
            this.returnedNodes = new HashSet<NodeID<T>>();
        }

        @Override
        public boolean hasNext() {
            if (returnedNodes.size() >= limit)
                return false;
            GraphCursor<T> cursor = getNextCursor();
            if (cursor == null)
                return false;
            return cursor.hasNext();
        }

        @Override
        public GraphEdge<T> next() {
            GraphEdge<T> next = getNextCursor().next();
            returnedNodes.add(next.getEndNode());
            return next;
        }

        @Override
        public GraphEdge<T> peekNext() {
            return getNextCursor().peekNext();
        }

        private GraphCursor<T> getNextCursor() {
            // The next cursor is gotten by peeking at each cursors next
            // returnable edge and selecting the one which has the highest
            // weight/priority.
            GraphCursor<T> res = null;
            float maxWeight = -1f;
            outer: for (GraphCursor<T> cursor : cursors) {
                if (!cursor.hasNext())
                    continue;
                GraphEdge<T> edge = cursor.peekNext();
                if (returnedNodes.contains(edge.getEndNode())) {
                    while (true) {
                        cursor.next(); // Move past the already returned node
                        if (!cursor.hasNext())
                            continue outer; // No more edges, skip this cursor
                        edge = cursor.peekNext();
                        if (!returnedNodes.contains(edge.getEndNode()))
                            break; // We found a valid node
                    }
                }
                // TODO: Make it possible to apply a function to the weight
                // TODO: based on the start node?
                float weight = edge.getWeight();
                if (weight > maxWeight) {
                    res = cursor;
                    maxWeight = weight;
                }
            }
            return res;
        }

        @Override
        public void close() {
            for (GraphCursor<T> cursor : cursors)
                cursor.close();
        }

        @Override
        public boolean isClosed() {
            for (GraphCursor<T> cursor : cursors) {
                if (!cursor.isClosed())
                    return false;
            }
            return true;
        }

        @Override
        public int getReturnedEdgeCount() {
            int returnedCount = 0;
            for (GraphCursor<T> cursor : cursors)
                returnedCount += cursor.getReturnedEdgeCount();
            return returnedCount;
        }

        @Override
        public int getTraversedEdgeCount() {
            int traversedCount = 0;
            for (GraphCursor<T> cursor : cursors)
                traversedCount += cursor.getTraversedEdgeCount();
            return traversedCount;
        }

        @Override
        public int currentDepth() {
            return getNextCursor().currentDepth();
        }
    }

    @Override
    protected Graph<T> getGraph() {
        if (startNodes.isEmpty())
            return null;
        return startNodes.get(0).getGraph();
    }
}
