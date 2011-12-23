package recng.graph;

/**
 * A weighted edge in a graph.
 *
 * @author jon
 */
public class TraversableGraphEdge<T>
    extends WeightedEdgeImpl<GraphNode<T>, GraphNode<T>>
    implements WeightedEdge<GraphNode<T>, GraphNode<T>> {

    public TraversableGraphEdge(GraphNode<T> startNode,
                                GraphNode<T> endNode,
                                EdgeType type, float weight) {
        super(startNode, endNode, type, weight);
    }
}