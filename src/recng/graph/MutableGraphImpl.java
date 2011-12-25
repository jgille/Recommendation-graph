package recng.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import recng.common.Consumer;

import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * A mutable graph.
 *
 * @author jon
 *
 */
public class MutableGraphImpl<T> implements MutableGraph<T> {

    /** Maps node id -> internal primary key */
    private final TObjectIntHashMap<NodeID<T>> nodeIndex =
        new TObjectIntHashMap<NodeID<T>>();
    /** All nodes in the graph. */
    private final ArrayList<MutableGraphNode<T>> nodes =
        new ArrayList<MutableGraphNode<T>>();
    /** Used for synchronization. */
    private final Object lock = new Object();
    private final GraphMetadata metadata;

    /**
     * Creates an empty mutable graph.
     *
     */
    public MutableGraphImpl(GraphMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * A class that can be used to build a mutable graph.
     *
     * @author jon
     */
    public static class Builder<T> implements GraphBuilder<T> {

        private final MutableGraphImpl<T> graph;

        public Builder(GraphMetadata metadata) {
            this.graph = new MutableGraphImpl<T>(metadata);
        }

        @Override
        public int addNode(NodeID<T> node) {
            return graph.upsertNode(node);
        }

        @Override
        public void addEdge(int startNodeIndex,
                            int endNodeIndex,
                            EdgeType edgeType,
                            float weight) {
            NodeID<T> startNode = graph.getNode(startNodeIndex).getNodeId();
            NodeID<T> endNode = graph.getNode(endNodeIndex).getNodeId();
            graph.addEdge(startNode, endNode, edgeType, weight);
        }

        @Override
        public Graph<T> build() {
            return graph;
        }
    }

    @Override
    public TraverserBuilder<T> prepareTraversal(NodeID<T> source,
                                                EdgeType edgeType) {
        if (source == null)
            throw new IllegalArgumentException("Null source node not allowed");
        if (edgeType == null)
            throw new IllegalArgumentException("Null edge type not allowed");

        int index = getPrimaryKey(source);
        MutableGraphNode<T> startNode = getNode(index);
        return new TraverserBuilderImpl<T>(startNode, edgeType);
    }

    @Override
    public void getAllEdges(Consumer<GraphEdge<T>, Void> consumer) {
        List<MutableGraphNode<T>> nodesCopy;
        synchronized (lock) {
            // Make a copy of the node list to avoid concurrency issues
            nodesCopy =
                new ArrayList<MutableGraphNode<T>>(nodes);
        }
        for (MutableGraphNode<T> node : nodesCopy) {
            for (EdgeType edgeType : metadata.getEdgeTypes()) {
                Iterator<TraversableGraphEdge<T>> it =
                    node.traverseNeighbors(edgeType);
                while (it.hasNext()) {
                    TraversableGraphEdge<T> edge = it.next();
                    GraphNode<T> startNode = edge.getStartNode();
                    GraphNode<T> endNode = edge.getEndNode();
                    if (startNode == null || endNode == null)
                        continue;
                    consumer.consume(new GraphEdge<T>
                        (startNode.getNodeId(), endNode.getNodeId(),
                         edgeType, edge.getWeight()));
                }
            }
        }
    }

    @Override
    public int nodeCount() {
        synchronized (lock) {
            return nodes.size();
        }
    }

    @Override
    public int edgeCount() {
        int edgeCount = 0;
        synchronized (lock) {
            for (MutableGraphNode<T> node : nodes)
                edgeCount += node.getEdgeCount();
        }
        return edgeCount;
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

    private int getPrimaryKey(NodeID<T> nodeId) {
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
    private MutableGraphNode<T> getNode(int primaryKey) {
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
        if (nodeIndex.contains(nodeId))
            return nodeIndex.get(nodeId);
        MutableGraphNode<T> node = new Node(nodeId);
        int index;
        index = nodes.size();
        nodeIndex.put(nodeId, index);
        nodes.add(node);
        return index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("** MutableGraphImpl **\n");
        sb.append("Node count: ").append(nodeCount());
        sb.append("Edge count: ").append(edgeCount());
        return sb.toString();
    }

    @Override
    public GraphMetadata getMetadata() {
        return metadata;
    }

    private class Node extends AbstractMutableGraphNode<T>
        implements MutableGraphNode<T> {

        private Node(NodeID<T> id) {
            super(id);
        }

        @Override
        protected int getPrimaryKey(NodeID<T> node) {
            return MutableGraphImpl.this.getPrimaryKey(node);
        }

        @Override
        protected GraphNode<T> getNode(int primaryKey) {
            return MutableGraphImpl.this.getNode(primaryKey);
        }
    }
}
