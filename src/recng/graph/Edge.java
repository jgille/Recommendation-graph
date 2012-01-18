package recng.graph;

/**
 * An edge in a graph.
 *
 * @author Jon Ivmark
 */
public interface Edge<U, V> {

    U getStartNode();

    V getEndNode();

    EdgeType getType();
}
