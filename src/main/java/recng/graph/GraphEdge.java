package recng.graph;

/**
 * A weighted edge in a graph.
 * 
 * @author jon
 */
public class GraphEdge<T> extends WeightedEdgeImpl<NodeID<T>> {
    public GraphEdge(NodeID<T> startNode,
                     NodeID<T> endNode,
                     EdgeType type, float weight) {
        super(startNode, endNode, type, weight);
    }
}
