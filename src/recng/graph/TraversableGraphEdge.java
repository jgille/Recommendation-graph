package recng.graph;

/**
 * A weighted edge in a graph.
 *
 * @author jon
 */
public class TraversableGraphEdge<K> extends
    WeightedEdgeImpl<GraphNode<K>, GraphNode<K>>
    implements WeightedEdge<GraphNode<K>, GraphNode<K>> {
    public TraversableGraphEdge(GraphNode<K> startNode,
                                GraphNode<K> endNode,
                                EdgeType type, float weight) {
        super(startNode, endNode, type, weight);
    }
}