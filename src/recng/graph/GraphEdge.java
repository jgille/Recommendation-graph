package recng.graph;

/**
 * A weighted edge in a graph.
 *
 * @author jon
 */
public class GraphEdge<T> extends WeightedEdgeImpl<NodeId<T>, NodeId<T>>
    implements WeightedEdge<NodeId<T>, NodeId<T>> {
    public GraphEdge(NodeId<T> startNode,
                     NodeId<T> endNode,
                     EdgeType type, float weight) {
        super(startNode, endNode, type, weight);
    }
}
