package recng.graph;

/**
 * A cursor returned from a graph traversal.
 * 
 * @author jon
 * 
 */
public interface GraphCursor<T> {

    /**
     * Returns true if there are any more edges in this cursor.
     */
    boolean hasNext();

    /**
     * Gets the next available edge.
     * 
     */
    GraphEdge<T> next();

    /**
     * Returns the next edge without iterating passed it, i.e. the next call to
     * {@link GraphCursor#peekNext()} or {@link GraphCursor#next()} will return
     * the same edge again.
     */
    GraphEdge<T> peekNext();

    /**
     * Closes the cursor. Should always be called in a finally block.
     */
    void close();

    /**
     * Checks if this cursor has been closed.
     */
    boolean isClosed();

    /**
     * Gets the number of returned edges by this cursor.
     */
    int getReturnedEdgeCount();

    /**
     * Gets the number of traversed edges by this cursor.
     */
    int getTraversedEdgeCount();

    /**
     * Gets the current depth, i.e. the number of edges between the start node
     * and the current one.
     * 
     */
    int currentDepth();

}