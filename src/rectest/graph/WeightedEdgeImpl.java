package rectest.graph;

/**
 * Base implementation of a weighted edge in a graph.
 *
 * @author Jon Ivmark
 */
public class WeightedEdgeImpl<U, V> extends EdgeImpl<U, V> implements WeightedEdge<U, V> {
    private final float weight;

    public WeightedEdgeImpl(U startNode, V endNode,
                            EdgeType type, float weight) {
        super(startNode, endNode, type);
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }

    @Override public String toString() {
        return String.format("%s -> %s, weight: %s", getStartNode(), getEndNode(), weight);
    }
}
