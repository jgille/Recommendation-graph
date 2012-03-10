package recng.graph;

/**
 * A weighted edge in a graph.
 * 
 * @author Jon Ivmark
 */
public interface WeightedEdge<T> extends Edge<T> {
    float getWeight();
}
