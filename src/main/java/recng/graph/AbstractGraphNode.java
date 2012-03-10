package recng.graph;

import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.mahout.math.list.LongArrayList;

/**
 * A node in a graph. Each node has a {@link NodeID} and an array of out edge
 * lists, one list per edge type. Edges are stored as longs where the first 4
 * bytes represent the edge weight as a float and the last 4 bytes represents
 * the primary key of the end node as an int.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the node IDs.
 */
public abstract class AbstractGraphNode<T> implements GraphNode<T> {
    private final NodeID<T> id;
    private final Graph<T> container;

    /**
     * The edges originating from this node. Contains one list of edges for each
     * edge type, using an offset into the array corresponding to
     * {@link EdgeType#ordinal()}.
     */
    private LongArrayList[] outEdges;

    public AbstractGraphNode(Graph<T> container, NodeID<T> id) {
        this.container = container;
        this.id = id;
    }

    public AbstractGraphNode(Graph<T> container, NodeID<T> id,
                             LongArrayList[] outEdges) {
        this(container, id);
        this.outEdges = outEdges;
    }

    @Override
    public Graph<T> getGraph() {
        return container;
    }

    protected Iterator<TraversableGraphEdge<T>> traverseEdges(EdgeType edgeType,
                                                              LongArrayList edges) {
        if (edges== null) // No out edges for this type
            return new EmptyIterator<TraversableGraphEdge<T>>();
        return new NeighborIterator(getNodeId(), edgeType, edges);
    }

    protected void forEachEdge(LongArrayList edges, final NodeIDProcedure<T> proc) {
        GraphStats status = getGraph().getStats();
        status.incTraversals();
        if (edges == null)
            return;
        int traversed = 0;
        for (int i = edges.size() - 1; i >= 0; i--) {
            long edge = edges.get(i);
            int endNodeIndex = getEndNodeIndex(edge);
            GraphNode<T> endNode = getGraph().getNode(endNodeIndex);
            if (!proc.apply(endNode.getNodeId()))
                break;
            traversed++;
        }
        status.incTraversedEdges(traversed);
    }

    protected LongArrayList[] getOutEdges() {
        return outEdges;
    }

    /**
     * The end node is in the last 4 bytes of the out edge.
     */
    protected static int getEndNodeIndex(long edge) {
        return (int) (edge & 0x7fffffff);
    }

    protected void setOutEdges(LongArrayList[] outEdges) {
        this.outEdges = outEdges;
    }

    /**
     * The weight is in the first 4 bytes of the out edge.
     */
    protected static float getWeight(long edge) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt((int) (edge >> 32));
        return buffer.getFloat(0);
    }

    /**
     * Gets the out edges of a certain type.
     */
    protected LongArrayList getOutEdges(EdgeType edgeType) {
        if (edgeType == null || outEdges == null)
            return null;
        int edgeTypeIndex = edgeType.ordinal();
        if (edgeTypeIndex < 0 || edgeTypeIndex >= outEdges.length)
            return null; // No edges for this type found
        return outEdges[edgeTypeIndex];
    }

    @Override
    public NodeID<T> getNodeId() {
        return id;
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
    protected class NeighborIterator implements
    Iterator<TraversableGraphEdge<T>> {

        private final int startNodeIndex;
        private final EdgeType edgeType;
        private final LongArrayList edges;
        private int currentIndex;

        public NeighborIterator(NodeID<T> startNode,
                                EdgeType edgeType,
                                LongArrayList edges) {
            this.startNodeIndex = getGraph().getPrimaryKey(startNode);
            this.edgeType = edgeType;
            this.edges = edges;
            this.currentIndex = edges.size() - 1;
        }

        @Override
        public boolean hasNext() {
            return currentIndex >= 0;
        }

        @Override
        public TraversableGraphEdge<T> next() {
            long edge = edges.get(currentIndex);
            int endNodeIndex = getEndNodeIndex(edge);
            float weight = getWeight(edge);
            GraphNode<T> start = getGraph().getNode(startNodeIndex);
            GraphNode<T> end = getGraph().getNode(endNodeIndex);
            currentIndex--;
            return new TraversableGraphEdge<T>(start, end, edgeType, weight);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
