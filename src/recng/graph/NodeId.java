package recng.graph;

/**
 * An identifier for a node in a graph.
 *
 * @author jon
 *
 * @param <K>
 */
public class NodeId<K> {

    private final K id;
    private final NodeType type;

    /**
     * Creates a node id, uniquely identified by the combination of the id and
     * type.
     *
     * @param id
     *            The id.
     * @param type
     *            The node type.
     */
    public NodeId(K id, NodeType type) {
        this.id = id;
        this.type = type;
    }

    public K getId() {
        return id;
    }

    public NodeType getNodeType() {
        return type;
    }

    @Override
    public int hashCode() {
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
        NodeId<K> node = (NodeId<K>) obj;
        return id.equals(node.id) && type.equals(node.type);
    }

    @Override
    public String toString() {
        return String.format("Id: %s, Type: %s", id, type.name());
    }

}
