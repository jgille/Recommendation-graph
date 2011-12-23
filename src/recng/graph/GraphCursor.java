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
     * @return
     */
    GraphEdge<T> next();

    /**
     * Closes the cursor. Should always be called in a finally block.
     */
    void close();

    /**
     * Checks if this cursor has been closed.
     */
    boolean isClosed();

}