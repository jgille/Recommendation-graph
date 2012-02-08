package recng.graph;

/**
 * Describes the type of an edge in a graph.
 *
 * Should normally be an enum where the EnumSet contains all of the valid edge
 * types for the graph.
 *
 * @author jon
 *
 */
public interface EdgeType {
    /**
     * Gets the name of this edge type.
     */
    String name();

    /**
     * Returns the ordinal of this edge type (it's position in the graphs set of
     * edge types).
     */
    int ordinal();

    boolean isWeighted();
}