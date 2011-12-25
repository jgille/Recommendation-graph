package recng.graph;

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
     * Returns the ordinal of this node type (it's position in the graphs set of
     * node types).
     */
    int ordinal();
}
