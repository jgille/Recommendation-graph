package recng.graph;

/**
 * A weighted edge in a graph.
 *
 * @author jon
 */
public class GraphEdge<K> extends WeightedEdgeImpl<NodeId<K>, NodeId<K>>
    implements WeightedEdge<NodeId<K>, NodeId<K>> {
    public GraphEdge(NodeId<K> startNode,
                     NodeId<K> endNode,
                     EdgeType type, float weight) {
        super(startNode, endNode, type, weight);
    }
}
