package recng.graph;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.AbstractObjectIntMap;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

/**
 * Base class for building graphs where edges are stored as longs, the first 4
 * bytes representing the edge weight and the last 4 bytes the end node index.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the node IDs.
 */
public abstract class AbstractGraphBuilder<T> implements GraphBuilder<T> {

    private final GraphMetadata metadata;
    // Node id -> primary key mapping
    private final AbstractObjectIntMap<NodeID<T>> nodeIndex =
        new OpenObjectIntHashMap<NodeID<T>>();
    // All node ids
    private final List<NodeID<T>> nodes = new ArrayList<NodeID<T>>();
    // Out edges by node and edge type
    private final List<LongArrayList[]> edges = new ArrayList<LongArrayList[]>();
    // Keeps track of the number of added edges
    private int edgeCount = 0;

    public AbstractGraphBuilder(GraphMetadata metadata) {
        this.metadata = metadata;
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
     * Adds a node if not already present.
     */
    private int addOrGet(NodeID<T> node) {
        if (nodeIndex.containsKey(node))
            return nodeIndex.get(node);
        int index = nodes.size();
        nodeIndex.put(node, index);
        nodes.add(node);
        edges.add(new LongArrayList[0]);
        return index;
    }

    @Override
    public int addOrGetNode(NodeID<T> node) {
        return addOrGet(node);
    }

    @Override
    public int getNodeIndex(NodeID<T> node) {
        if (nodeIndex.containsKey(node))
            return nodeIndex.get(node);
        return -1;
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
        LongArrayList[] outEdges = edges.get(startNodeIndex);
        // Expand array if necessary
        if (outEdges.length <= ordinal) {
            outEdges = Arrays.copyOf(outEdges, ordinal + 1);
            // Since the array has been recreated we need to set it in the
            // list
            edges.set(startNodeIndex, outEdges);
        }
        // Get the edges for this edge type
        LongArrayList typedEdges = outEdges[ordinal];
        // Create and store the internal edge representation
        long edge = createOutEdge(endNodeIndex, weight);
        if (typedEdges == null) {
            typedEdges = new LongArrayList();
            outEdges[ordinal] = typedEdges;
        }
        typedEdges.add(edge);
        edgeCount++;
    }

    @Override
    public Graph<T> build() {
        // Sort all out edges on edge weight (since stored in the first 4
        // bytes, just sorting on the entire long is fine)
        for (LongArrayList[] edgeArrays : edges) {
            for (LongArrayList outEdges : edgeArrays) {
                if (outEdges == null)
                    continue;
                outEdges.trimToSize();
                outEdges.sort();
            }
        }
        return constructGraph(metadata, nodeIndex, nodes, edges);
    }

    /**
     * Creates a graph.
     *
     * @param metadata
     *            The graph metadata.
     * @param nodeIndex
     *            Maps node id -> node index
     * @param nodes
     *            All nodes.
     * @param nodeEdges
     *            All out edges for each node.
     * @return
     */
    protected abstract Graph<T>
    constructGraph(GraphMetadata metadata,
                   AbstractObjectIntMap<NodeID<T>> nodeIndex,
                   List<NodeID<T>> nodes,
                   List<LongArrayList[]> nodeEdges);
}
