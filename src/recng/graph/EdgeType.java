package recng.graph;

/**
 * Describes the type of an edge in a graph.
 * 
 * @author jon
 * 
 */
public interface EdgeType {
    /**
     * Gets the name of this edge type.
     */
    String name();

    boolean isWeighted();
}