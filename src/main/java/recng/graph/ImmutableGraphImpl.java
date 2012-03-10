package recng.graph;

import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.AbstractObjectIntMap;

import recng.graph.jmx.JMXGraph;
import recng.jmx.JMXUtils;

/**
 * A graph containing nodes with weighted edges to other nodes.
 *
 * @author Jon Ivmark
 */
public class ImmutableGraphImpl<T> extends AbstractGraph<T> {

    /** Index node id -> internal primary key */
    private final AbstractObjectIntMap<NodeID<T>> nodeIndex;
    /** All nodes in the graph */
    private final List<GraphNode<T>> nodes;

    private ImmutableGraphImpl(GraphMetadata metadata,
                               AbstractObjectIntMap<NodeID<T>> nodeIndex,
                               List<NodeID<T>> nodes,
                               List<LongArrayList[]> edges) {
        super(metadata);
        this.nodeIndex = nodeIndex;
        this.nodes = new ArrayList<GraphNode<T>>();
        int i = 0;
        for (NodeID<T> nodeId : nodes) {
            LongArrayList[] nodeEdges = edges.get(i++);
            if (nodeEdges == null) {
                throw new IllegalArgumentException("Null edge array for node "
                    + nodeId);
            }
            LongArrayList[] edgeLists = new LongArrayList[nodeEdges.length];
            int j = 0;
            for (LongArrayList el : nodeEdges) {
                if (el == null) {
                    edgeLists[j] = new LongArrayList(0);
                } else {
                    edgeLists[j] = el;
                }
                j++;
            }
            GraphNode<T> node = new ImmutableGraphNodeImpl<T>(this, nodeId, edgeLists);
            this.nodes.add(node);
        }
        JMXUtils.registerMBean(new JMXGraph<T>(this));
    }

    @Override
    protected List<GraphNode<T>> getNodes() {
        return nodes;
    }

    @Override
    public int nodeCount() {
        return nodes.size();
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
        if (!nodeIndex.containsKey(id))
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
                       AbstractObjectIntMap<NodeID<T>> nodeIndex,
                       List<NodeID<T>> nodes,
                       List<LongArrayList[]> nodeEdges) {
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