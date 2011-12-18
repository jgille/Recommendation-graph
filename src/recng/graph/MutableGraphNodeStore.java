package recng.graph;

/**
 * A store of mutable nodes, typically a mutable graph,
 *
 * @author jon
 *
 * @param <K>
 */
public interface MutableGraphNodeStore<K> {

    int getPrimaryKey(NodeId<K> nodeId);

    MutableGraphNode<K> getNode(int primaryKey);
}
