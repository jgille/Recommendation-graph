package recng.graph;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import recng.common.Consumer;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.list.array.TLongArrayList;

/**
 * A graph containing nodes with weighted edges to other nodes.
 *
 * @author Jon Ivmark
 */
public class GraphImpl<T> implements Graph<T> {

    /** Index node id -> internal primary key */
    private final TObjectIntHashMap<NodeID<T>> nodeIndex;
    /** All nodes in the graph */
    private final ArrayList<Node> nodes = new ArrayList<Node>();
    private final GraphMetadata metadata;

    private GraphImpl(GraphMetadata metadata,
                      TObjectIntHashMap<NodeID<T>> nodeIndex,
                      ArrayList<NodeID<T>> nodes,
                      ArrayList<TLongArrayList[]> edges) {
        this.metadata = metadata;
        this.nodeIndex = nodeIndex;
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
     * Each out edge for a node is internally stored as a long, where the first
     * 4 bytes represent the edge weight and the las 4 bytes represents the end
     * node index.
     *
     * This method creates such an edge representation.
     */
    private static long createOutEdge(int endNode, float weight) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putFloat(weight);
        buffer.putInt(endNode);
        return buffer.getLong(0);
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
        return new TraverserBuilderImpl<T>(getNode(index), edgeType);
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
            return GraphImpl.this.getNodeIndex(node);
        }

        @Override
        protected GraphNode<T> getNode(int primaryKey) {
            return GraphImpl.this.getNode(primaryKey);
        }
    }

    /**
     * A class used to build a graph.
     */
    public static class Builder<T> implements GraphBuilder<T> {
        private final GraphMetadata metadata;
        // Node id -> primary key mapping
        private final TObjectIntHashMap<NodeID<T>> nodeIndex =
            new TObjectIntHashMap<NodeID<T>>();
        // All node ids
        private final ArrayList<NodeID<T>> nodes = new ArrayList<NodeID<T>>();
        // Out edges by node and edge type
        private final ArrayList<TLongArrayList[]> edges =
            new ArrayList<TLongArrayList[]>();
        // Keeps track of the number of added edges
        private int edgeCount = 0;

        public Builder(GraphMetadata metadata) {
            this.metadata = metadata;
        }

        /**
         * Adds a node if not already present.
         */
        private int upsert(NodeID<T> node) {
            if (nodeIndex.contains(node))
                return nodeIndex.get(node);
            int index = nodes.size();
            nodeIndex.put(node, index);
            nodes.add(node);
            edges.add(new TLongArrayList[metadata.getEdgeTypes().size()]);
            return index;
        }

        @Override
        public int addNode(NodeID<T> node) {
            return upsert(node);
        }

        /**
         * Adds an edge.
         */
        @Override
        public void addEdge(int startNodeIndex, int endNodeIndex,
                            EdgeType edgeType, float weight) {
            // The location of edges of a certain edge type is defines by the
            // ordinal of the edge type.
            int ordinal = edgeType.ordinal();
            TLongArrayList[] outEdges = edges.get(startNodeIndex);
            // Expand array if necessary
            if (outEdges.length <= ordinal) {
                outEdges = Arrays.copyOf(outEdges, ordinal + 1);
                // Since the array has been recreated we need to set it in the
                // list
                edges.set(startNodeIndex, outEdges);
            }
            // Get the edges for this edge type
            TLongArrayList typedEdges = outEdges[ordinal];
            // Create and store the internal edge representation
            long edge = GraphImpl.createOutEdge(endNodeIndex, weight);
            if (typedEdges == null) {
                typedEdges = new TLongArrayList();
                outEdges[ordinal] = typedEdges;
            }
            typedEdges.add(edge);
            edgeCount++;
        }

        @Override
        public Graph<T> build() {
            // Sort all out edges on edge weight (since stored in the first 4
            // bytes, just sorting on the entire long is fine)
            for (TLongArrayList[] edgeArrays : edges) {
                for (TLongArrayList outEdges : edgeArrays) {
                    if (outEdges == null)
                        continue;
                    outEdges.trimToSize();
                    outEdges.sort();
                }
            }
            return new GraphImpl<T>(metadata, nodeIndex, nodes, edges);
        }
    }

    @Override
    public GraphMetadata getMetadata() {
        return metadata;
    }
}