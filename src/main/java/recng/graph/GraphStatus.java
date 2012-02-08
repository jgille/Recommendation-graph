package recng.graph;

import java.util.Date;

/**
 * Describes the current status of a graph.
 *
 * @author jon
 *
 */
public interface GraphStatus {

    /**
     * Gets the time this graph was created,
     */
    Date getInitTime();

    /**
     * Gets the number of traversal requests.
     */
    int getNumberOfTraversals();

    /**
     * Increments the number of traversal requests by 1.
     */
    void incNumberOfTraversals();

    /**
     * Gets the number of edges requested from the traversals.
     */
    long getRequestedEdges();

    /**
     * Increments the number of reqeusted edges by the delta.
     */
    void incRequestedEdges(int delta);

    /**
     * Gets the number of edges returned from the traversals.
     */
    int getReturnedEdges();

    /**
     * Increments the number of returned edges by the delta.
     */
    void incReturnedEdges(int delta);

    /**
     * Gets the number of edges traversed in the traversals.
     */
    int getTraversedEdges();

    /**
     * Gets the max number of edges traversed in a single traversal.
     */
    int getMaxTraversedEdges();

    /**
     * Increments the number of traversed edges by the delta.
     */
    void incTraversedEdges(int delta);

    /**
     * Gets the total time, in ms, spent traversing the graph.
     *
     * NOTE: This might be > (now - init time) in a multi threaded environment.
     */
    long getTraversalTime();

    /**
     * Increments the total time, in ms, spent traversing the graph by the
     * delta.
     */
    void incTraversalTime(long delta);

    /**
     * Gets the maximum traversal time, in ms.
     */
    long getMaxTraversalTime();
}
