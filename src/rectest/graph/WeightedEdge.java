package rectest.graph;

/**
 * A weighted edge in a graph.
 *
 * @author Jon Ivmark
 */
public interface WeightedEdge<U, V> extends Edge<U, V> {
    float getWeight();
}
