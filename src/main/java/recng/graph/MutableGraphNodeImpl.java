package recng.graph;

import java.nio.ByteBuffer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mahout.math.function.LongProcedure;
import org.apache.mahout.math.list.LongArrayList;

/**
 * An implementation of a mutable graph node.
 *
 * @author jon
 *
 * @param <T>
 *            The type of the key for this node.
 */
public class MutableGraphNodeImpl<T> extends AbstractGraphNode<T>
    implements MutableGraphNode<T> {

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
     * Created a new mutable node.
     *
     * @param id
     *            The id of this node.
     */
    public MutableGraphNodeImpl(Graph<T> container, NodeID<T> id) {
        super(container, id);
    }

    /**
     * Created a new mutable node.
     *
     * @param id
     *            The id of this node.
     */
    public MutableGraphNodeImpl(Graph<T> container, NodeID<T> id,
                                LongArrayList[] outEdges) {
        super(container, id, outEdges);
    }

    @Override
    public Iterator<TraversableGraphEdge<T>>
        traverseNeighbors(EdgeType edgeType) {
        LongArrayList edges;
        synchronized (this) {
            edges = getOutEdges(edgeType);
            if (edges != null)
                edges = edges.copy();
        }
        return traverseEdges(edgeType, edges);
    }

    @Override
    public void forEachNeighbor(EdgeType edgeType, final NodeIDProcedure<T> proc) {
        LongArrayList edges;
        synchronized (this) {
            edges = getOutEdges(edgeType);
            if (edges != null)
                edges = edges.copy();
        }
        forEachEdge(edges, proc);
    }

    @Override
    public synchronized int getEdgeCount() {
        LongArrayList[] outEdges = getOutEdges();
        int edgeCount = 0;
        if (outEdges == null)
            return edgeCount;
        for (LongArrayList edges : outEdges) {
            if (edges != null)
                edgeCount += edges.size();
        }
        return edgeCount;
    }

    @Override
    public synchronized void addEdge(int endNodeIndex, EdgeType edgeType,
                                     float weight) {
        long edge = createOutEdge(endNodeIndex, weight);
        LongArrayList edges = getOutEdges(edgeType);
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
                edges.beforeInsert(index, edge);
            }
        }
    }

    @Override
    public synchronized boolean updateEdge(int endNodeIndex,
                                           EdgeType edgeType,
                                           float weight) {
        int index = findEdge(edgeType, endNodeIndex);
        if (index < 0) // Non existing edge
            return false;
        LongArrayList edges = getOutEdges(edgeType);
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

    private void shiftLeft(LongArrayList edges, int offset, int length) {
        for (int i = 0; i < length; i++)
            edges.set(offset + i - 1, edges.get(offset + i));
    }

    private void shiftRight(LongArrayList edges, int offset, int length) {
        for (int i = offset + length - 1; i >= offset; i--) {
            if (i < edges.size() - 1)
                edges.set(i + 1, edges.get(i));
            else
                edges.add(edges.get(i));
        }
    }

    @Override
    public synchronized boolean removeEdge(int endNodeIndex,
                                           EdgeType edgeType) {
        int index = findEdge(edgeType, endNodeIndex);
        if (index < 0)
            return false; // Non existing edge
        LongArrayList edges = getOutEdges(edgeType);
        edges.remove(index);
        return true;
    }

    @Override
    public synchronized void setEdges(EdgeType edgeType,
                                      List<Integer> endNodes,
                                      List<Float> weights) {
        int edgeTypeIndex = edgeType.ordinal();
        LongArrayList edges = new LongArrayList(endNodes.size());
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
        LongArrayList[] outEdges = getOutEdges();
        if (outEdges == null) {
            outEdges = new LongArrayList[edgeTypeIndex + 1];
            setOutEdges(outEdges);
        } else if (edgeTypeIndex >= outEdges.length) {
            outEdges = Arrays.copyOf(outEdges, edgeTypeIndex + 1);
            setOutEdges(outEdges);
        }
        outEdges[edgeTypeIndex] = edges;
    }

    /**
     * Find an edge by the primary key of it's end node.
     *
     * Performed by a (potentially) full scan of the edge list.
     */
    private int findEdge(EdgeType edgeType, final int endNodeIndex) {
        LongArrayList edges = getOutEdges(edgeType);
        if (edges == null)
            return -1;
        final AtomicInteger index = new AtomicInteger(0);
        final AtomicBoolean found = new AtomicBoolean(false);
        edges.forEach(new LongProcedure() {

            @Override
            public boolean apply(long edge) {
                int nodeIndex = getEndNodeIndex(edge);
                if (nodeIndex == endNodeIndex) {
                    found.set(true);
                    return false;
                }
                index.incrementAndGet();
                return true;
            }
        });
        if (found.get())
            return index.get();
        return -1;
    }

    /**
     * Finds the appropriate index (offset) for an edge based on it's weight.
     * Since edges are sorted on weight, we use a binary search here.
     */
    private int findIndex(EdgeType edgeType, float weight) {
        LongArrayList edges = getOutEdges(edgeType);
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
}
