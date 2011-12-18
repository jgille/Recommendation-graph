package rectest.graph;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import rectest.common.Consumer;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TLongArrayList;

/**
 * A graph containing nodes with weighted edges to other nodes.
 *
 * @author Jon Ivmark
 */
public class GraphImpl<K> implements Graph<K> {

    /** Index node id -> internal primary key */
    private final TObjectIntHashMap<NodeId<K>> nodeIndex;
    /** All nodes in the graph */
    private final ArrayList<Node> nodes = new ArrayList<Node>();
    /** All unique edge types that this graph contains. */
    private final Map<EdgeType, Integer> edgeTypes;

    private GraphImpl(TObjectIntHashMap<NodeId<K>> nodeIndex,
                      ArrayList<NodeId<K>> nodes,
                      ArrayList<TLongArrayList[]> edges,
                      Map<EdgeType, Integer> edgeTypes) {
        this.nodeIndex = nodeIndex;
        this.edgeTypes = edgeTypes;
        int i = 0;
        for (NodeId<K> nodeId : nodes) {
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
     * The end node is in the last 4 bytes of the out edge.
     */
    private static int getEndNodeIndex(long edge) {
        return (int) (edge & 0xffff);
    }

    /**
     * The weight is in the last 4 bytes of the out edge.
     */
    private static float getWeight(long edge) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt((int) (edge >> 32));
        return buffer.getFloat(0);
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
    private int getNodeIndex(NodeId<K> id) {
        if (!nodeIndex.contains(id))
            return -1;
        return nodeIndex.get(id);
    }

    /**
     * All edge types currently in this graph.
     */
    private Set<EdgeType> getEdgeTypes() {
        return edgeTypes.keySet();
    }

    @Override
    public TraverserBuilder<K> prepareTraversal(NodeId<K> source,
                                                EdgeType edgeType) {
        int index = getNodeIndex(source);
        return new TraverserBuilderImpl<K>(getNode(index), edgeType);
    }

    @Override
    public void
        getEdges(Consumer<GraphEdge<K>, Void> consumer) {
        for (Node node : nodes) {
            for (EdgeType edgeType : getEdgeTypes()) {
                Iterator<TraversableGraphEdge<K>> it =
                    node.traverseNeighbors(edgeType);
                while (it.hasNext()) {
                    TraversableGraphEdge<K> edge = it.next();
                    consumer.consume(new GraphEdge<K>
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
     * A node in the graph. Each node contains an id and an array of edge lists,
     * one such list for each edge type.
     *
     * Individual (out) edges for the node are stored as longs, where these
     * longs contains both the primary key of the end node and the edge weight.
     */
    private class Node implements GraphNode<K> {
        private final NodeId<K> id;
        private final TLongArrayList[] edges;

        private Node(NodeId<K> id, TLongArrayList[] edges) {
            this.id = id;
            this.edges = edges;
        }

        @Override
        public Iterator<TraversableGraphEdge<K>>
            traverseNeighbors(EdgeType edgeType) {
            TLongArrayList edgeList = getEdges(edgeType);
            if (edgeList == null) // No out edges for this type
                return new EmptyIterator<TraversableGraphEdge<K>>();
            return new NeighborIterator(GraphImpl.this.getNodeIndex(id),
                                        edgeType,
                                        edgeList.iterator());
        }

        @Override
        public NodeId<K> getNodeId() {
            return id;
        }

        @Override
        public String toString() {
            // Build a string representation of the first N out edges
            StringBuilder sb = new StringBuilder("N: ").append(getNodeId());
            for (EdgeType edgeType : GraphImpl.this.getEdgeTypes()) {
                int i = 0;
                Iterator<TraversableGraphEdge<K>> it =
                    traverseNeighbors(edgeType);
                while (it.hasNext() && i++ < 5) {
                    TraversableGraphEdge<K> edge = it.next();
                    NodeId<K> end = edge.getEndNode().getNodeId();
                    sb.append("\n  - t: ").append(edgeType)
                        .append(", w: ").append(edge.getWeight())
                        .append(" -> N: ").append(end);
                }

                if (it.hasNext())
                    sb.append("\n  Has more...");
            }
            return sb.toString();
        }

        /**
         * Gets out edges for an edge type.
         */
        private TLongArrayList getEdges(EdgeType edgeType) {
            if (!edgeTypes.containsKey(edgeType))
                return null;
            int ordinal = edgeTypes.get(edgeType);
            if (ordinal >= edges.length)
                return null;
            return edges[ordinal];
        }

        /**
         * Gets the total number of out edges for this node.
         */
        private int getEdgeCount() {
            int edgeCount = 0;
            for (TLongArrayList outEdges : edges) {
                if (outEdges != null)
                    edgeCount += outEdges.size();
            }
            return edgeCount;
        }
    }

    /**
     * An iterator used to iterate over the immediate out edges for a node.
     */
    private class NeighborIterator implements
        Iterator<TraversableGraphEdge<K>> {

        private final int startNodeIndex;
        private final EdgeType edgeType;
        private final TLongIterator edges;

        public NeighborIterator(int startNodeIndex, EdgeType edgeType,
                                TLongIterator edges) {
            this.startNodeIndex = startNodeIndex;
            this.edgeType = edgeType;
            this.edges = edges;
        }

        @Override
        public boolean hasNext() {
            return edges.hasNext();
        }

        @Override
        public TraversableGraphEdge<K> next() {
            long edge = edges.next();
            int endNodeIndex = GraphImpl.getEndNodeIndex(edge);
            float weight = GraphImpl.getWeight(edge);
            Node start = GraphImpl.this.getNode(startNodeIndex);
            Node end = GraphImpl.this.getNode(endNodeIndex);
            return new TraversableGraphEdge<K>(start, end, edgeType, weight);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A class used to build a graph.
     */
    public static class Builder<K> implements GraphBuilder<K> {

        // Node id -> primary key mapping
        private final TObjectIntHashMap<NodeId<K>> nodeIndex =
            new TObjectIntHashMap<NodeId<K>>();
        // All node ids
        private final ArrayList<NodeId<K>> nodes = new ArrayList<NodeId<K>>();
        // Out edges by node and edge type
        private final ArrayList<TLongArrayList[]> edges =
            new ArrayList<TLongArrayList[]>();
        // All unique edge types
        private final Map<EdgeType, Integer> edgeTypes =
            new HashMap<EdgeType, Integer>();
        // Keeps track of the number of added edges
        private int edgeCount = 0;
        // Keeps track of the edge type ordinal
        private int maxOrdinal = 0;

        @Override
        public GraphBuilder<K> addEdge(NodeId<K> from, NodeId<K> to,
                                       EdgeType edgeType,
                                       float weight) {
            // Add the nodes if necessary
            int fromIndex = upsert(from);
            int toIndex = upsert(to);
            // Add the edge type and edge
            if (!edgeTypes.containsKey(edgeType))
                edgeTypes.put(edgeType, maxOrdinal++);
            addEdge(fromIndex, toIndex, edgeType, weight);
            edgeCount++;
            return this;
        }

        /**
         * Adds a node if not already present.
         */
        private int upsert(NodeId<K> node) {
            if (nodeIndex.contains(node))
                return nodeIndex.get(node);
            int index = nodes.size();
            nodeIndex.put(node, index);
            nodes.add(node);
            edges.add(new TLongArrayList[edgeTypes.size()]);
            return index;
        }

        /**
         * Adds an edge.
         */
        private void addEdge(int from, int to, EdgeType edgeType, float weight) {
            // The location of edges of a certain edge type is defines by the
            // ordinal of the edge type.
            int ordinal = edgeTypes.get(edgeType);
            TLongArrayList[] outEdges = edges.get(from);
            // Expand array if necessary
            if (outEdges.length <= ordinal) {
                outEdges = Arrays.copyOf(outEdges, ordinal + 1);
                // Since the array has been recreated we need to set it in the
                // list
                edges.set(from, outEdges);
            }
            // Get the edges for this edge type
            TLongArrayList typedEdges = outEdges[ordinal];
            // Create and store the internal edge representation
            long edge = GraphImpl.createOutEdge(to, weight);
            if (typedEdges == null) {
                typedEdges = new TLongArrayList();
                outEdges[ordinal] = typedEdges;
            }
            typedEdges.add(edge);
        }

        @Override
        public Graph<K> build() {
            // Sort all out edges on edge weight (since stored in the first 4
            // bytes, just sorting on the entire long is fine)
            for (TLongArrayList[] edgeArrays : edges) {
                for (TLongArrayList outEdges : edgeArrays) {
                    if (outEdges == null)
                        continue;
                    outEdges.trimToSize();
                    outEdges.sort();
                    outEdges.reverse();
                }
            }
            return new GraphImpl<K>(nodeIndex, nodes, edges, edgeTypes);
        }
    }
}