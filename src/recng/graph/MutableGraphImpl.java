package recng.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import recng.common.Consumer;

import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * A mutable graph.
 *
 * @author jon
 *
 */
public class MutableGraphImpl<K> implements MutableGraph<K> {

    /** Maps node id -> internal primary key */
    private final TObjectIntHashMap<NodeId<K>> nodeIndex =
        new TObjectIntHashMap<NodeId<K>>();
    /** All nodes in the graph. */
    private final ArrayList<MutableGraphNode<K>> nodes =
        new ArrayList<MutableGraphNode<K>>();
    /** Used for synchronization. */
    private final Object lock = new Object();
    /** All unique edge types that this graph contains. */
    private final Set<EdgeType> edgeTypes;

    /**
     * Creates an empty mutable graph.
     *
     */
    public MutableGraphImpl(Set<EdgeType> edgeTypes) {
        this.edgeTypes = edgeTypes;
    }

    /**
     * A class that can be used to build a mutable graph.
     *
     * @author jon
     */
    public static class Builder<K> implements GraphBuilder<K> {

        private final MutableGraph<K> graph;

        public Builder(Set<EdgeType> edgeTypes) {
            this.graph = new MutableGraphImpl<K>(edgeTypes);
        }

        @Override
        public GraphBuilder<K> addEdge(NodeId<K> from,
                                       NodeId<K> to,
                                       EdgeType edgeType,
                                       float weight) {
            graph.addEdge(from, to, edgeType, weight);
            return this;
        }

        @Override
        public Graph<K> build() {
            return graph;
        }
    }

    @Override
    public TraverserBuilder<K> prepareTraversal(NodeId<K> source,
                                                EdgeType edgeType) {
        if (source == null)
            throw new IllegalArgumentException("Null source node not allowed");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        int index = getPrimaryKey(source);
        MutableGraphNode<K> startNode = getNode(index);
        return new TraverserBuilderImpl<K>(startNode, edgeType);
    }

    @Override
    public void
        getEdges(Consumer<GraphEdge<K>, Void> consumer) {
        List<MutableGraphNode<K>> nodesCopy;
        synchronized (lock) {
            // Make a copy of the node list to avoid concurrency issues
            nodesCopy =
                new ArrayList<MutableGraphNode<K>>(nodes);
        }
        for (MutableGraphNode<K> node : nodesCopy) {
            for (EdgeType edgeType : getEdgeTypes()) {
                Iterator<TraversableGraphEdge<K>> it =
                    node.traverseNeighbors(edgeType);
                while (it.hasNext()) {
                    TraversableGraphEdge<K> edge = it.next();
                    GraphNode<K> startNode = edge.getStartNode();
                    GraphNode<K> endNode = edge.getEndNode();
                    if (startNode == null || endNode == null)
                        continue;
                    consumer.consume(new GraphEdge<K>
                        (startNode.getNodeId(), endNode.getNodeId(),
                         edgeType, edge.getWeight()));
                }
            }
        }
    }

    @Override
    public int nodeCount() {
        return nodes.size();
    }

    @Override
    public int edgeCount() {
        int edgeCount = 0;
        // TODO: Add synchronization?
        for (MutableGraphNode<K> node : nodes)
            edgeCount += node.getEdgeCount();
        return edgeCount;
    }

    @Override
    public void addEdge(NodeId<K> start, NodeId<K> end, EdgeType edgeType,
                        float weight) {
        if (start == null || end == null)
            throw new IllegalArgumentException("Null nodes not allowed");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        MutableGraphNode<K> startNode;
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
    public boolean updateEdge(NodeId<K> start, NodeId<K> end,
                              EdgeType edgeType, float weight) {
        if (start == null || end == null)
            throw new IllegalArgumentException("Null nodes not allowed");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        MutableGraphNode<K> startNode;
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
    public boolean removeEdge(NodeId<K> start, NodeId<K> end,
                              EdgeType edgeType) {
        if (start == null || end == null)
            throw new IllegalArgumentException("Null nodes not allowed");

        MutableGraphNode<K> startNode;
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
    public void setEdges(NodeId<K> start, EdgeType edgeType,
                         List<NodeId<K>> endNodes, List<Float> weights) {
        if (endNodes == null || weights == null)
            throw new IllegalArgumentException("Null edge lists not allowed");
        if (endNodes.size() != weights.size())
            throw new IllegalArgumentException("Mismatch between end node " +
                "and weight count");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        MutableGraphNode<K> startNode;
        List<Integer> endNodeIndexes = new ArrayList<Integer>();
        synchronized (lock) { // Create start and end nodes if necessary
            int startNodeIndex = upsertNode(start);
            startNode = getNode(startNodeIndex);
            for (NodeId<K> endNode : endNodes) {
                int index = upsertNode(endNode);
                endNodeIndexes.add(index);
            }
        }
        // Set the edges
        startNode.setEdges(edgeType, endNodeIndexes, weights);
    }

    @Override
    public int getPrimaryKey(NodeId<K> nodeId) {
        synchronized (lock) {
            if (!nodeIndex.contains(nodeId))
                return -1;
            return nodeIndex.get(nodeId);
        }
    }

    /**
     * Gets a node by it's primary key. You should not modify a node gotten with
     * this method, to modify it use the method in this class
     * (add/update/removeEdge etc). Modifying the node directly may lead to
     * concurrency issues.
     */
    @Override
    public MutableGraphNode<K> getNode(int primaryKey) {
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
    private int upsertNode(NodeId<K> nodeId) {
        if (nodeId == null)
            return -1;
        if (nodeIndex.contains(nodeId))
            return nodeIndex.get(nodeId);
        MutableGraphNode<K> node = new MutableGraphNodeImpl<K>(nodeId, this);
        int index;
        index = nodes.size();
        nodeIndex.put(nodeId, index);
        nodes.add(node);
        return index;
    }

    /**
     * All edge types currently in this graph.
     */
    private Set<EdgeType> getEdgeTypes() {
        return edgeTypes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("** MutableGraphImpl **");
        int i = 0;
        for (MutableGraphNode<K> node : nodes) {
            sb.append("\n").append(node.toVerboseString());
            if (i++ > 20) {
                sb.append("\nHas more...");
                break;
            }
        }
        return sb.toString();
    }
}
