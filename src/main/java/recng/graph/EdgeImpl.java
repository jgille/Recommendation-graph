package recng.graph;

/**
 * Base implementation of an edge in a graph.
 *
 * @author Jon Ivmark
 */
public class EdgeImpl<T> implements Edge<T> {
    private final T startNode;
    private final T endNode;
    private final EdgeType type;

    public EdgeImpl(T startNode, T endNode, EdgeType type) {
        if (startNode == null || endNode == null || type == null)
            throw new IllegalArgumentException("Null arguments not allowed");
        this.startNode = startNode;
        this.endNode = endNode;
        this.type = type;
    }

    public T getStartNode() {
        return startNode;
    }

    public T getEndNode() {
        return endNode;
    }

    public EdgeType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + endNode.hashCode();
        result =
            prime * result + startNode.hashCode();
        result = prime * result + type.hashCode();
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
        EdgeImpl<T> other = (EdgeImpl<T>) obj;
        return startNode.equals(other.startNode)
            && endNode.equals(other.endNode)
            && type.equals(other.type);
    }

    @Override public String toString() {
        return String.format("%s -> %s", getStartNode(), getEndNode());
    }

}
