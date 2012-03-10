package recng.graph;

import java.util.Set;

/**
 * Metadata about the valid edge and node types for a graph.
 *
 * @author jon
 */
public interface GraphMetadata {

    /**
     * Gets all valid edge types for a graph.
     */
    Set<EdgeType> getEdgeTypes();

    /**
     * Gets all valid node types for a graph.
     */
    Set<NodeType> getNodeTypes();
}
