package recng.graph;

/**
 *
 * A cursor returned from a graph traversal.
 *
 * @author jon
 *
 */
class GraphCursorImpl<T> implements GraphCursor<T>  {
    private boolean isClosed = false;
    private final GraphIterator<T> iterator;

    /**
     * Creates a cursor from an iterator.
     */
    public GraphCursorImpl(GraphIterator<T> iterator) {
        this.iterator = iterator;
    }

    public boolean hasNext() {
        if (isClosed)
            throw new IllegalStateException("This cursor has been closed");
        return iterator.hasNext();
    }

    public GraphEdge<T> next() {
        if (isClosed)
            throw new IllegalStateException("This cursor has been closed");
        return iterator.next();
    }

    public void close() {
        isClosed = true;
    }

    public boolean isClosed() {
        return isClosed;
    }
}
