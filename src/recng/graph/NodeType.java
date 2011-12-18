package recng.graph;

import java.util.Map;

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
     *
     * The integer values connected to each edge type are used as indexes in
     * edge arrays and should be both unique and sequential (starting at 0).
     */
    Map<EdgeType, Integer> validEdgeTypes();

    int indexOf(EdgeType edgeType);

}
