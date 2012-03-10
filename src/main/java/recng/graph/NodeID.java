package recng.graph;

/**
 * An identifier for a node in a graph.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the id.
 */
public class NodeID<T> {

    private final T id;
    private final NodeType type;
    private final int hc;

    /**
     * Creates a node id, uniquely identified by the combination of the id and
     * type.
     *
     * @param id
     *            The id.
     * @param type
     *            The node type.
     */
    public NodeID(T id, NodeType type) {
        this.id = id;
        this.type = type;
        this.hc = computeHashCode();
    }

    public T getID() {
        return id;
    }

    public NodeType getNodeType() {
        return type;
    }

    @Override
    public int hashCode() {
        return hc;
    }

    private int computeHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + type.hashCode();
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        NodeID<T> node = (NodeID<T>) obj;
        return id.equals(node.id) && type.equals(node.type);
    }

    @Override
    public String toString() {
        return String.format("Id: %s, Type: %s", id, type.name());
    }

}
