package recng.graph;

/**
 * An edge in a graph.
 *
 * @author Jon Ivmark
 */
public interface Edge<T> {

    T getStartNode();

    T getEndNode();

    EdgeType getType();
}
