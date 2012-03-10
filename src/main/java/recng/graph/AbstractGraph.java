package recng.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for weighted graphs.
 * 
 * @author jon
 * 
 * @param <T>
 *            The generic type of the node ids.
 */
abstract class AbstractGraph<T> implements Graph<T> {

    private final GraphMetadata metadata;
    private final GraphStats status;

    public AbstractGraph(GraphMetadata metadata) {
        this.metadata = metadata;
        this.status = new GraphStatsImpl();
    }

    protected abstract List<GraphNode<T>> getNodes();

    @Override
    public void forEachNode(NodeIDProcedure<T> proc) {
        for (GraphNode<T> node : getNodes())
            proc.apply(node.getNodeId());
    }

    @Override
    public void forEachNode(NodeIDProcedure<T> proc, NodeType nodeType) {
        for (GraphNode<T> node : getNodes())
            if (node.getNodeId().getNodeType().equals(nodeType)) {
                if (!proc.apply(node.getNodeId()))
                    break;
            }
    }

    @Override
    public void forEachNeighbor(NodeID<T> source, EdgeType edgeType, NodeIDProcedure<T> proc) {
        if (source == null)
            throw new IllegalArgumentException("Null source node not allowed");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        int index = getPrimaryKey(source);
        GraphNode<T> startNode = getNode(index);
        if (startNode != null)
            startNode.forEachNeighbor(edgeType, proc);
    }

    @Override
    public void forEachEdge(GraphEdgeProcedure<T> proc) {
        for (GraphNode<T> node : getNodes()) {
            for (EdgeType edgeType : getMetadata().getEdgeTypes()) {
                Iterator<TraversableGraphEdge<T>> it =
                    node.traverseNeighbors(edgeType);
                while (it.hasNext()) {
                    TraversableGraphEdge<T> edge = it.next();
                    GraphNode<T> startNode = edge.getStartNode();
                    GraphNode<T> endNode = edge.getEndNode();
                    if (startNode == null || endNode == null)
                        continue;
                    GraphEdge<T> graphEdge =
                        new GraphEdge<T>(startNode.getNodeId(), endNode.getNodeId(),
                                         edgeType, edge.getWeight());
                    if (!proc.apply(graphEdge))
                        break;
                }
            }
        }
    }

    @Override
    public Traverser<T> getTraverser(NodeID<T> source,
                                     EdgeType edgeType) {
        if (source == null)
            throw new IllegalArgumentException("Null source node not allowed");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        int index = getPrimaryKey(source);
        GraphNode<T> startNode = getNode(index);
        if (startNode == null)
            return null;
        return new TraverserImpl<T>(this, startNode, edgeType);
    }

    @Override
    public Traverser<T> getMultiTraverser(List<NodeID<T>> sources,
                                          EdgeType edgeType) {
        if (sources == null)
            throw new IllegalArgumentException("Null source nodes not allowed");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        List<GraphNode<T>> sourceNodes = new ArrayList<GraphNode<T>>();
        for (NodeID<T> id : sources) {
            int index = getPrimaryKey(id);
            if (index < 0)
                continue;
            sourceNodes.add(getNode(index));

        }
        if (sourceNodes.isEmpty())
            return null;
        return new MultiTraverser<T>(this, sourceNodes, edgeType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("** Graph **");
        int i = 0;
        sb.append("\nNode count: ").append(nodeCount()).append("\n");
        sb.append("Edge count: ").append(edgeCount()).append("\n");
        for (GraphNode<T> node : getNodes()) {
            sb.append("\n").append(node);
            for (EdgeType edgeType : getMetadata().getEdgeTypes()) {
                Iterator<TraversableGraphEdge<T>> edges =
                    node.traverseNeighbors(edgeType);
                int j = 0;
                while (edges.hasNext() && j++ < 5) {
                    TraversableGraphEdge<T> edge = edges.next();
                    sb.append("\n\t -- ").append(edge.getType())
                        .append(" --> ").append(edge.getEndNode().getNodeId())
                        .append(" (w=").append(edge.getWeight()).append(")");
                }
                if (edges.hasNext())
                    sb.append("\n\tHas more...");
            }

            if (i++ > 5) {
                sb.append("\nHas more...");
                break;
            }
        }
        sb.append("\nStatus:\n").append(status.toString());
        return sb.toString();
    }

    @Override
    public int edgeCount() {
        int edgeCount = 0;
        for (GraphNode<T> node : getNodes())
            edgeCount += node.getEdgeCount();
        return edgeCount;
    }

    @Override
    public GraphMetadata getMetadata() {
        return metadata;
    }

    @Override
    public GraphStats getStats() {
        return status;
    }
}
