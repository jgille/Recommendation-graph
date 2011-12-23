package recng.graph;

import java.util.Set;

/**
 * Describes a type of node in a graph.
 *
 * @author jon
 *
 */
public interface NodeType {

    /**
     * Gets the node type name.
     */
    String name();

    /**
     * Gets the valid edge types of edges originating from nodes of this type.
     */
    Set<EdgeType> validEdgeTypes();

    /**
     * Gets the index of an edge type. Must return a value between 0 and
     * validEdgeTypes().size() - 1 for all valid edge types, and must return
     * unique indexes for all valid edge types. For invalid edge types, -1 is
     * returned.
     */
    int indexOf(EdgeType edgeType);

}
