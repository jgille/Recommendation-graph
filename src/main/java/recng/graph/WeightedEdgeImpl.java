package recng.graph;

/**
 * Base implementation of a weighted edge in a graph.
 * 
 * @author Jon Ivmark
 */
public class WeightedEdgeImpl<T> extends EdgeImpl<T> implements WeightedEdge<T> {
    private final float weight;

    public WeightedEdgeImpl(T startNode, T endNode,
                            EdgeType type, float weight) {
        super(startNode, endNode, type);
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return String.format("{%s -> %s, weight: %s}", getStartNode(),
                             getEndNode(), weight);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getEndNode().hashCode();
        result =
            prime * result + getStartNode().hashCode();
        result = prime * result + getType().hashCode();
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WeightedEdgeImpl<T> other = (WeightedEdgeImpl<T>) obj;
        return getStartNode().equals(other.getStartNode())
            && getEndNode().equals(other.getEndNode())
            && getType().equals(other.getType());
    }

}
