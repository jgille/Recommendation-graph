package recng.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import recng.common.Consumer;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.list.array.TLongArrayList;

/**
 * A graph containing nodes with weighted edges to other nodes.
 *
 * @author Jon Ivmark
 */
public class ImmutableGraphImpl<T> implements ImmutableGraph<T> {

    /** Index node id -> internal primary key */
    private final TObjectIntHashMap<NodeID<T>> nodeIndex;
    /** All nodes in the graph */
    private final List<Node> nodes;
    /** Metadata about this graph. */
    private final GraphMetadata metadata;

    private ImmutableGraphImpl(GraphMetadata metadata,
                               TObjectIntHashMap<NodeID<T>> nodeIndex,
                               List<NodeID<T>> nodes,
                               List<TLongArrayList[]> edges) {
        this.metadata = metadata;
        this.nodeIndex = nodeIndex;
        this.nodes = new ArrayList<Node>();
        int i = 0;
        for (NodeID<T> nodeId : nodes) {
            TLongArrayList[] nodeEdges = edges.get(i);
            TLongArrayList[] edgeLists = new TLongArrayList[nodeEdges.length];
            int j = 0;
            for (TLongArrayList el : nodeEdges) {
                if (el == null) {
                    edgeLists[j] = new TLongArrayList(0);
                } else {
                    edgeLists[j] = el;
                }
                j++;
            }
            Node node = new Node(nodeId, edgeLists);
            this.nodes.add(node);
            i++;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("** GraphImpl **");
        int i = 0;
        for (Node node : nodes) {
            sb.append("\n").append(node);
            if (i++ > 20) {
                sb.append("\nHas more...");
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Gets a node from it's primary key.
     */
    private Node getNode(int index) {
        if (index >= nodes.size())
            return null;
        return nodes.get(index);
    }

    /**
     * Gets the primary key for a node.
     */
    private int getNodeIndex(NodeID<T> id) {
        if (!nodeIndex.contains(id))
            return -1;
        return nodeIndex.get(id);
    }

    @Override
    public TraverserBuilder<T> prepareTraversal(NodeID<T> source,
                                                EdgeType edgeType) {
        int index = getNodeIndex(source);
        if (index < 0)
            return null;
        return new TraverserBuilderImpl<T>(getNode(index), edgeType);
    }

    @Override
    public void getAllNodes(Consumer<NodeID<T>, Void> consumer) {
        for (Node node : nodes)
            consumer.consume(node.getNodeId());
    }

    @Override
    public void
        getAllEdges(Consumer<GraphEdge<T>, Void> consumer) {
        for (Node node : nodes) {
            for (EdgeType edgeType : metadata.getEdgeTypes()) {
                Iterator<TraversableGraphEdge<T>> it =
                    node.traverseNeighbors(edgeType);
                while (it.hasNext()) {
                    TraversableGraphEdge<T> edge = it.next();
                    consumer.consume(new GraphEdge<T>
                        (edge.getStartNode().getNodeId(),
                         edge.getEndNode().getNodeId(),
                         edgeType, edge.getWeight()));
                }
            }
        }
    }

    @Override
    public int nodeCount() {
        return nodeIndex.size();
    }

    @Override
    public int edgeCount() {
        int edgeCount = 0;
        for (Node node : nodes)
            edgeCount += node.getEdgeCount();
        return edgeCount;
    }

    /**
     * A node in the graph.
     */
    private class Node extends AbstractGraphNode<T> implements GraphNode<T> {

        private Node(NodeID<T> id, TLongArrayList[] edges) {
            super(id, edges);
        }

        @Override
        protected int getPrimaryKey(NodeID<T> node) {
            return ImmutableGraphImpl.this.getPrimaryKey(node);
        }

        @Override
        protected GraphNode<T> getNode(int primaryKey) {
            return ImmutableGraphImpl.this.getNode(primaryKey);
        }
    }

    /**
     * A class used to build a graph.
     */
    public static class Builder<T> extends AbstractGraphBuilder<T> implements
        GraphBuilder<T> {

        public Builder(GraphMetadata metadata) {
            super(metadata);
        }

        @Override
        protected Graph<T>
            constructGraph(GraphMetadata metadata,
                           TObjectIntHashMap<NodeID<T>> nodeIndex,
                           List<NodeID<T>> nodes,
                           List<TLongArrayList[]> nodeEdges) {
            return new ImmutableGraphImpl<T>(metadata, nodeIndex, nodes, nodeEdges);
        }
    }

    @Override
    public GraphMetadata getMetadata() {
        return metadata;
    }

    @Override
    public int getPrimaryKey(NodeID<T> nodeID) {
        return getNodeIndex(nodeID);
    }
}