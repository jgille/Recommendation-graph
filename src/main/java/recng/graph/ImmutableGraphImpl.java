package recng.graph;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.list.array.TLongArrayList;

/**
 * A graph containing nodes with weighted edges to other nodes.
 *
 * @author Jon Ivmark
 */
public class ImmutableGraphImpl<T> extends AbstractGraph<T> {

    /** Index node id -> internal primary key */
    private final TObjectIntHashMap<NodeID<T>> nodeIndex;
    /** All nodes in the graph */
    private final List<GraphNode<T>> nodes;

    private ImmutableGraphImpl(GraphMetadata metadata,
                               TObjectIntHashMap<NodeID<T>> nodeIndex,
                               List<NodeID<T>> nodes,
                               List<TLongArrayList[]> edges) {
        super(metadata);
        this.nodeIndex = nodeIndex;
        this.nodes = new ArrayList<GraphNode<T>>();
        int i = 0;
        for (NodeID<T> nodeId : nodes) {
            TLongArrayList[] nodeEdges = edges.get(i);
            if (nodeEdges == null || nodeEdges.length == 0)
                continue;
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
            GraphNode<T> node = new GraphNodeImpl<T>(this, nodeId, edgeLists);
            this.nodes.add(node);
            i++;
        }
    }

    @Override
    protected List<GraphNode<T>> getNodes() {
        return nodes;
    }

    /**
     * Gets a node from it's primary key.
     */
    @Override
    public GraphNode<T> getNode(int index) {
        if (index < 0 || index >= nodes.size())
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

    /**
     * A class used to build a graph.
     */
    public static class Builder<T> extends AbstractGraphBuilder<T> {

        private Builder(GraphMetadata metadata) {
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

        public static <T> Builder<T> create(GraphMetadata metadata) {
            return new Builder<T>(metadata);
        }
    }

    @Override
    public int getPrimaryKey(NodeID<T> nodeID) {
        return getNodeIndex(nodeID);
    }
}