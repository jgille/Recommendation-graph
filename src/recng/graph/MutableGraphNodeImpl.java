package recng.graph;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TLongArrayList;
import java.nio.ByteBuffer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of a mutable graph node.
 *
 * @author jon
 *
 * @param <T>
 *            The type of the key for this node.
 */
public class MutableGraphNodeImpl<T> implements MutableGraphNode<T> {

    private final MutableGraphNodeStore<T> nodeStore;
    private final NodeID<T> id;
    private TLongArrayList[] outEdges = null;

    /**
     * Each out edge for a node is internally stored as a long, where the first
     * 4 bytes represent the edge weight and the last 4 bytes represents the end
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
     * Created a new mutable node.
     *
     * @param id
     *            The id of this node.
     * @param nodeStore
     *            The container for this node and it's neighbors
     */
    public MutableGraphNodeImpl(NodeID<T> id, MutableGraphNodeStore<T> nodeStore) {
        this.id = id;
        this.nodeStore = nodeStore;
    }

    /**
     * Gets the out edges of a certain type.
     */
    private TLongArrayList getOutEdges(EdgeType edgeType) {
        if (edgeType == null || outEdges == null)
            return null;
        int edgeTypeIndex = id.getNodeType().indexOf(edgeType);
        if (edgeTypeIndex < 0 || edgeTypeIndex >= outEdges.length)
            return null; // No edges for this type found
        return outEdges[edgeTypeIndex];
    }

    @Override
    public synchronized int getEdgeCount() {
        int edgeCount = 0;
        if (outEdges == null)
            return edgeCount;
        for (TLongArrayList edges : outEdges) {
            if (edges != null)
                edgeCount += edges.size();
        }
        return edgeCount;
    }

    @Override
    public synchronized Iterator<TraversableGraphEdge<T>>
    traverseNeighbors(EdgeType edgeType) {
        TLongArrayList edgeList = getOutEdges(edgeType);
        if (edgeList == null) // No out edges for this type
            return new EmptyIterator<TraversableGraphEdge<T>>();
        // return a copy of the edges to avoid concurrency issues
        long[] edgeArray = edgeList.toArray();
        return new NeighborIterator<T>(nodeStore, id, edgeType, edgeArray);
    }

    public NodeID<T> getNodeId() {
        return id;
    }

    @Override
    public synchronized void addEdge(int endNodeIndex, EdgeType edgeType,
                                     float weight) {
        long edge = createOutEdge(endNodeIndex, weight);
        TLongArrayList edges = getOutEdges(edgeType);
        if (edges == null) { // First edge of this type, set the edge array
                             // accordingly
            setEdges(edgeType, Collections.singletonList(endNodeIndex),
                     Collections.singletonList(weight));
        } else {
            if (!edgeType.isWeighted()) {
                // For unweighted edges there is no need to
                // keep things sorted
                edges.add(edge);
            } else {
                // Finds the appropriate index at which to insert this edge
                // (based on edge weight)
                int index = findIndex(edgeType, weight);
                edges.insert(index, edge);
            }
        }
    }

    public synchronized boolean updateEdge(int endNodeIndex,
                                           EdgeType edgeType,
                                           float weight) {
        int index = findEdge(edgeType, endNodeIndex);
        if (index < 0) // Non existing edge
            return false;
        TLongArrayList edges = getOutEdges(edgeType);
        long edge = createOutEdge(endNodeIndex, weight);
        int newIndex = findIndex(edgeType, weight);
        // We (might) need to move the edge in the array to keep it sorted on
        // edge weight. In doing so, we need to shift other edges either to the
        // left or right in the array.
        if (newIndex > index) {
            int shiftFrom = index + 1;
            int shiftTo = newIndex;
            // Shift all edges in the sub array (old index + 1 .. new index) one
            // step to the left to make room for the updated edge
            shiftLeft(edges, shiftFrom, shiftTo - shiftFrom + 1);
        } else if (newIndex < index) {
            int shiftFrom = newIndex;
            int shiftTo = index - 1;
            // Shift all edges in the sub array (new index .. old index - 1) one
            // step to the right to make room for the updated edge
            shiftRight(edges, shiftFrom, shiftTo - shiftFrom + 1);
        }
        edges.set(newIndex, edge);
        return true;
    }

    private void shiftLeft(TLongArrayList edges, int offset, int length) {
        for (int i = 0; i < length; i++)
            edges.set(offset + i - 1, edges.get(offset + i));
    }

    private void shiftRight(TLongArrayList edges, int offset, int length) {
        for (int i = 0; i < length; i++)
            edges.set(offset + i + 1, edges.get(offset + i));
    }

    public synchronized boolean removeEdge(int endNodeIndex,
                                           EdgeType edgeType) {
        int index = findEdge(edgeType, endNodeIndex);
        if (index < 0)
            return false; // Non existing edge
        TLongArrayList edges = getOutEdges(edgeType);
        edges.remove(index, 1);
        return true;
    }

    public synchronized void setEdges(EdgeType edgeType,
                                      List<Integer> endNodes,
                                      List<Float> weights) {
        int edgeTypeIndex = id.getNodeType().indexOf(edgeType);
        if (edgeTypeIndex < 0)
            return;

        TLongArrayList edges = new TLongArrayList(endNodes.size());
        int i = 0;
        // Create edge list
        for (int endNode : endNodes) {
            float weight = weights.get(i++);
            long edge = createOutEdge(endNode, weight);
            edges.add(edge);
        }
        if (edgeType.isWeighted()) // Keep weighted edges sorted
            edges.sort();
        // Create/expand array if necessary
        if (outEdges == null) {
            outEdges = new TLongArrayList[edgeTypeIndex + 1];
        } else if (edgeTypeIndex >= outEdges.length) {
            outEdges = Arrays.copyOf(outEdges, edgeTypeIndex + 1);
        }
        outEdges[edgeTypeIndex] = edges;
    }

    /**
     * Find an edge by the primary key of it's end node.
     *
     * Performed by a (potentially) full scan of the edge list.
     */
    private int findEdge(EdgeType edgeType, int endNodeIndex) {
        TLongArrayList edges = getOutEdges(edgeType);
        if (edges == null)
            return -1;
        TLongIterator it = edges.iterator();
        int index = 0;
        while (it.hasNext()) {
            long edge = it.next();
            int nodeIndex = getEndNodeIndex(edge);
            if (nodeIndex == endNodeIndex)
                return index;
            index++;
        }
        return -1;
    }

    /**
     * Finds the appropriate index (offset) for an edge based on it's weight.
     * Since edges are sorted on weight, we use a binary search here.
     */
    private int findIndex(EdgeType edgeType, float weight) {
        TLongArrayList edges = getOutEdges(edgeType);
        if (edges == null)
            return -1;
        int low = 0;
        int high = edges.size() - 1;
        int mid = -1;

        while (low <= high) {
            mid = (low + high) / 2;
            long edge = edges.get(mid);
            float edgeWeight = getWeight(edge);
            if (edgeWeight < weight)
                low = mid + 1;
            else if (edgeWeight > weight)
                high = mid - 1;
            else
                return mid;
        }
        long edge = edges.get(mid);
        float edgeWeight = getWeight(edge);
        if (edgeWeight < weight)
            return mid + 1;
        return mid;
    }

    public String toVerboseString() {
        // Return the id and at most 5 edges per edge type
        StringBuilder sb = new StringBuilder("N: ").append(getNodeId());
        for (EdgeType edgeType : id.getNodeType().validEdgeTypes()) {
            int i = 0;

            Iterator<TraversableGraphEdge<T>> it = traverseNeighbors(edgeType);
            while (it.hasNext() && i++ < 5) {
                TraversableGraphEdge<T> edge = it.next();
                NodeID<T> end =
                    edge.getEndNode() != null ? edge.getEndNode().getNodeId()
                        : null;
                    sb.append("\n  - t: ").append(edgeType)
                    .append(", w: ").append(edge.getWeight())
                    .append(" -> N: ").append(end);
            }

            if (it.hasNext())
                sb.append("\n  Has more...");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    /**
     * An iterator used to iterate over the immediate out edges for a node.
     *
     * Since edges are stored sorted by ascending weight, we iterate the edges
     * backwards.
     */
    private static class NeighborIterator<K> implements
        Iterator<TraversableGraphEdge<K>> {

        private final MutableGraphNodeStore<K> nodeStore;
        private final int startNodeIndex;
        private final EdgeType edgeType;
        private final long[] edges;
        private int currentIndex;

        public NeighborIterator(MutableGraphNodeStore<K> nodeStore,
                                NodeID<K> startNode,
                                EdgeType edgeType,
                                long[] edges) {
            this.nodeStore = nodeStore;
            this.startNodeIndex = nodeStore.getPrimaryKey(startNode);
            this.edgeType = edgeType;
            this.edges = edges;
            this.currentIndex = edges.length - 1;
        }

        @Override
        public boolean hasNext() {
            return currentIndex >= 0;
        }

        @Override
        public TraversableGraphEdge<K> next() {
            long edge = edges[currentIndex];
            int endNodeIndex = getEndNodeIndex(edge);
            float weight = getWeight(edge);
            GraphNode<K> start = nodeStore.getNode(startNodeIndex);
            GraphNode<K> end = nodeStore.getNode(endNodeIndex);
            currentIndex--;
            return new TraversableGraphEdge<K>(start, end, edgeType, weight);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
