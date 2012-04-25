package recng.graph;

import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.AbstractObjectIntMap;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

import recng.graph.jmx.JMXGraph;
import recng.jmx.JMXUtils;

/**
 * A mutable graph implementation.
 *
 * @author jon
 *
 */
public class MutableGraphImpl<T> extends AbstractGraph<T> implements
    MutableGraph<T> {

    /**
     * Maps node id -> internal primary key (index in of node in internal node
     * list)
     */
    private final AbstractObjectIntMap<NodeID<T>> nodeIndex;
    /** All nodes in the graph. */
    private final List<MutableGraphNode<T>> nodes;
    /** Used for synchronization. */
    private final Object lock = new Object();

    /**
     * Creates an empty mutable graph.
     */
    public MutableGraphImpl(GraphMetadata metadata) {
        this(metadata, new OpenObjectIntHashMap<NodeID<T>>(),
             new ArrayList<NodeID<T>>(),
             new ArrayList<LongArrayList[]>());
    }

    private MutableGraphImpl(GraphMetadata metadata,
                             AbstractObjectIntMap<NodeID<T>> nodeIndex,
                             List<NodeID<T>> nodes,
                             List<LongArrayList[]> edges) {
        super(metadata);
        this.nodeIndex = nodeIndex;
        this.nodes = new ArrayList<MutableGraphNode<T>>();
        int i = 0;
        for (NodeID<T> nodeId : nodes) {
            LongArrayList[] nodeEdges = edges.get(i);
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
            MutableGraphNode<T> node =
                new MutableGraphNodeImpl<T>(this, nodeId, edgeLists);
            this.nodes.add(node);
            i++;
        }
        JMXUtils.registerMBean(new JMXGraph<T>(this));
    }

    @Override
    protected List<GraphNode<T>> getNodes() {
        synchronized (lock) {
            return new ArrayList<GraphNode<T>>(nodes);
        }
    }

    @Override
    public int nodeCount() {
        synchronized (lock) {
            return nodes.size();
        }
    }

    @Override
    public void addEdge(NodeID<T> start, NodeID<T> end, EdgeType edgeType,
                        float weight) {
        if (start == null || end == null)
            throw new IllegalArgumentException("Null nodes not allowed");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        MutableGraphNode<T> startNode;
        int endNodeIndex;
        synchronized (lock) { // Create start and end nodes if necessary
            int startNodeIndex = upsertNode(start);
            startNode = getNode(startNodeIndex);
            endNodeIndex = upsertNode(end);
        }
        // Create the edge
        startNode.addEdge(endNodeIndex, edgeType, weight);
    }

    @Override
    public boolean updateEdge(NodeID<T> start, NodeID<T> end,
                              EdgeType edgeType, float weight) {
        if (start == null || end == null)
            throw new IllegalArgumentException("Null nodes not allowed");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        MutableGraphNode<T> startNode;
        int endNodeIndex;
        synchronized (lock) {
            // Make sure that both the start and end node exists
            int startNodeIndex = getPrimaryKey(start);
            if (startNodeIndex < 0)
                return false;
            endNodeIndex = getPrimaryKey(end);
            if (endNodeIndex < 0)
                return false;
            startNode = getNode(startNodeIndex);
        }
        // Update the edge
        return startNode.updateEdge(endNodeIndex, edgeType, weight);
    }

    @Override
    public boolean removeEdge(NodeID<T> start, NodeID<T> end,
                              EdgeType edgeType) {
        if (start == null || end == null)
            throw new IllegalArgumentException("Null nodes not allowed");

        MutableGraphNode<T> startNode;
        int endNodeIndex;
        synchronized (lock) {
            // Make sure that both the start and end node exists
            int startNodeIndex = getPrimaryKey(start);
            if (startNodeIndex < 0)
                return false;
            endNodeIndex = getPrimaryKey(end);
            if (endNodeIndex < 0)
                return false;
            startNode = getNode(startNodeIndex);
        }
        // Remove the edge
        return startNode.removeEdge(endNodeIndex, edgeType);
    }

    @Override
    public void setEdges(NodeID<T> start, EdgeType edgeType,
                         List<NodeID<T>> endNodes, List<Float> weights) {
        if (endNodes == null || weights == null)
            throw new IllegalArgumentException("Null edge lists not allowed");
        if (endNodes.size() != weights.size())
            throw new IllegalArgumentException("Mismatch between end node " +
                "and weight count");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        MutableGraphNode<T> startNode;
        List<Integer> endNodeIndexes = new ArrayList<Integer>();
        synchronized (lock) { // Create start and end nodes if necessary
            int startNodeIndex = upsertNode(start);
            startNode = getNode(startNodeIndex);
            for (NodeID<T> endNode : endNodes) {
                int index = upsertNode(endNode);
                endNodeIndexes.add(index);
            }
        }
        // Set the edges
        startNode.setEdges(edgeType, endNodeIndexes, weights);
    }

    @Override
    public int getPrimaryKey(NodeID<T> nodeId) {
        synchronized (lock) {
            if (!nodeIndex.containsKey(nodeId))
                return -1;
            return nodeIndex.get(nodeId);
        }
    }

    /**
     * Gets a node by it's primary key.
     */
    @Override
    public MutableGraphNode<T> getNode(int primaryKey) {
        synchronized (lock) {
            if (primaryKey >= nodes.size() || primaryKey < 0)
                return null;
            return nodes.get(primaryKey);
        }
    }

    /**
     * Creates a node if it does not already exist.
     *
     * Not thread safe and needs synchronization.
     */
    private int upsertNode(NodeID<T> nodeId) {
        if (nodeId == null)
            return -1;
        if (nodeIndex.containsKey(nodeId))
            return nodeIndex.get(nodeId);
        MutableGraphNode<T> node = new MutableGraphNodeImpl<T>(this, nodeId);
        int index;
        index = nodes.size();
        nodeIndex.put(nodeId, index);
        nodes.add(node);
        return index;
    }

    /**
     * A class used to build a graph.
     */
    public static class Builder<T> extends AbstractGraphBuilder<T> {

        public Builder(GraphMetadata metadata) {
            super(metadata);
        }

        @Override
        protected Graph<T>
            constructGraph(GraphMetadata metadata,
                           AbstractObjectIntMap<NodeID<T>> nodeIndex,
                           List<NodeID<T>> nodes,
                           List<LongArrayList[]> nodeEdges) {
            return new MutableGraphImpl<T>(metadata, nodeIndex, nodes,
                                           nodeEdges);
        }
    }
}
