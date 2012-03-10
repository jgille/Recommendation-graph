package recng.graph.jmx;

import java.util.Date;
import java.util.List;

public interface JMXGraphMBean {

    /**
     * Gets the number of nodes in this graph.
     */
    int getNodeCount();

    /**
     * Gets the number of nodes in this graph.
     */
    int getEdgeCount();

    /**
     * Gets the valid node types for this graph.
     */
    List<String> getNodeTypes();

    /**
     * Gets the valid edge types for this graph.
     */
    List<String> getEdgeTypes();

    /**
     * Gets the time this graph was created,
     */
    Date getInitTime();

    /**
     * Gets the number of traversal requests.
     */
    int getTraversals();

    /**
     * Gets the total number of edges traversed in the traversals.
     */
    int getTraversedEdges();

    /**
     * Gets the max number of edges traversed in a single traversal.
     */
    int getMaxTraversedEdges();

    /**
     * Gets the average number of edges traversed in the traversals.
     */
    int getAverageTraversedEdges();

}
