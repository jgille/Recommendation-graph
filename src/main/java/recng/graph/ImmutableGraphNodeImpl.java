package recng.graph;

import java.util.Iterator;

import org.apache.mahout.math.list.LongArrayList;

/**
 * An implementation of an immutable graph node.
 *
 * @author jon
 *
 * @param <T>
 *            The type of the key for this node.
 */
public class ImmutableGraphNodeImpl<T> extends AbstractGraphNode<T> {

    public ImmutableGraphNodeImpl(Graph<T> container, NodeID<T> id, LongArrayList[] outEdges) {
        super(container, id, outEdges);
    }

    @Override
    public Iterator<TraversableGraphEdge<T>>
        traverseNeighbors(EdgeType edgeType) {
        return traverseEdges(edgeType, getOutEdges(edgeType));
    }

    @Override
    public int getEdgeCount() {
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
    public void forEachNeighbor(EdgeType edgeType, final NodeIDProcedure<T> proc) {
        LongArrayList edges = getOutEdges(edgeType);
        forEachEdge(edges, proc);
    }
}
