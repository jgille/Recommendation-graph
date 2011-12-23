package recng.graph;

/**
 * A store of mutable nodes, typically a mutable graph,
 * 
 * @author jon
 * 
 * @param <T>
 *            The generic type of the node IDs.
 */
public interface MutableGraphNodeStore<T> {

    /**
     * Gets the primary key of a node, often this will be an offset into an
     * array of all nodes in the store.
     */
    int getPrimaryKey(NodeID<T> nodeId);

    /**
     * Gets a node by it's primary key. Returns null if no such node exists.
     */
    MutableGraphNode<T> getNode(int primaryKey);
}
