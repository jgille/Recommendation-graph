package recng.graph;

import java.util.Date;

/**
 * Describes the current status of a graph.
 *
 * @author jon
 *
 */
public interface GraphStats {

    /**
     * Gets the time this graph was created,
     */
    Date getInitTime();

    /**
     * Gets the number of traversal requests.
     */
    int getTraversals();

    /**
     * Increments the number of traversal requests by 1.
     */
    void incTraversals();

    /**
     * Gets the number of edges traversed in the traversals.
     */
    int getTraversedEdges();

    /**
     * Increments the number of traversed edges by the delta.
     */
    void incTraversedEdges(int delta);

    /**
     * Gets the max number of edges traversed in a single traversal.
     */
    int getMaxTraversedEdges();
}
