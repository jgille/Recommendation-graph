package recng.graph;

/**
 * 
 * A cursor returned from a graph traversal.
 * 
 * @author jon
 * 
 */
class GraphCursorImpl<T> implements GraphCursor<T> {
    private boolean isClosed = false;
    private final GraphIterator<T> iterator;

    /**
     * Creates a cursor from an iterator.
     */
    public GraphCursorImpl(GraphIterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        if (isClosed)
            throw new IllegalStateException("This cursor has been closed");
        return iterator.hasNext();
    }

    @Override
    public GraphEdge<T> next() {
        if (isClosed)
            throw new IllegalStateException("This cursor has been closed");
        return iterator.next();
    }

    @Override
    public GraphEdge<T> peekNext() {
        if (isClosed)
            throw new IllegalStateException("This cursor has been closed");
        return iterator.peekNext();
    }

    @Override
    public void close() {
        isClosed = true;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public int getReturnedEdgeCount() {
        return iterator.getReturnedEdgeCount();
    }

    @Override
    public int getTraversedEdgeCount() {
        return iterator.getTraversedEdgeCount();
    }

    @Override
    public String toString() {
        return "Cur: " + iterator.toString();
    }

    @Override
    public int currentDepth() {
        return iterator.currentDepth();
    }

}
