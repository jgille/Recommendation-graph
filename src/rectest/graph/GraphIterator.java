package rectest.graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * A class used to iterate edges starting from a node in a graph.
 *
 * The behavior of the iteration is defined by a set of rules where you define
 * things like how many edges to return, the maximum number of edges to iterate
 * before returning, the maximum depth from the start node to iterate etc.
 *
 * @author jon
 *
 * @param <K>
 *            The type of the node keys in the graph.
 */
public class GraphIterator<K> implements Iterator<GraphEdge<K>> {

    private final GraphNode<K> sourceNode;
    // Current out edges
    private Iterator<TraversableGraphEdge<K>> edges;
    // The type of edges to traverse
    private final EdgeType edgeType;
    // A filter used to decide if and edge should be returned or not
    private final EdgeFilter<K> returnableFilter;
    // The maximum depth (distance from the start node) to traverse
    private final int maxDepth;
    // The maximum number of edges to return
    private final int maxReturnedEdges;
    // The maximum number of edges to traverse
    private final int maxTraversedEdges;
    // The current depth in this traversal
    private int currentDepth = 1;
    // The number of edges that have been returned
    private int returnedEdgeCount = 0;
    // The number of edges that have been traversed
    private int traversedEdgeCount = 0;
    // The next edge to return
    private TraversableGraphEdge<K> currentEdge = null;
    // Queued nodes used when we want to traverse at a depth > 1
    private final LinkedList<NodeAndDepth<K>> neighborQueue =
        new LinkedList<NodeAndDepth<K>>();
    // Avoid loops
    private final Set<EdgeId<K>> traversedEdges = new HashSet<EdgeId<K>>();
    // Avoid duplicates among returned end nodes
    private final Set<NodeId<K>> returnedNodes = new HashSet<NodeId<K>>();
    // Used to avoid using the same start node twice
    private final Set<NodeId<K>> touchedNodes = new HashSet<NodeId<K>>();

    /**
     * Created an iterator originating at a start node.
     *
     * @param sourceNode
     *            The start of the iteration.
     * @param edgeType
     *            The type of edges to follow.
     * @param returnableFilter
     *            Used to filter out invalid edges.
     * @param maxDepth
     *            The maximum depth from the start node.
     * @param maxReturnedEdges
     *            The maximum number of edges to return.
     * @param maxTraversedEdges
     *            The maximum number of edges to traverse before returning.
     */
    GraphIterator(GraphNode<K> sourceNode, EdgeType edgeType,
                  EdgeFilter<K> returnableFilter, int maxDepth,
                  int maxReturnedEdges,
                  int maxTraversedEdges) {
        this.sourceNode = sourceNode;
        this.edges = sourceNode.traverseNeighbors(edgeType);
        this.edgeType = edgeType;
        this.returnableFilter = returnableFilter;
        this.maxDepth = maxDepth;
        this.maxReturnedEdges = maxReturnedEdges;
        this.maxTraversedEdges = maxTraversedEdges;
    }

    public boolean hasNext() {
        if (sourceNode == null)
            return false;
        return hasNext(edges);
    }

    private boolean hasNext(Iterator<TraversableGraphEdge<K>> edgeIterator) {
        if (returnedEdgeCount >= maxReturnedEdges)
            return false; // We've filled the quota
        if (traversedEdgeCount >= maxTraversedEdges)
            return false; // We've traversed too many edges
        if (currentDepth > maxDepth)
            return false; // We've traversed too deeply

        // Iterate immediate out edges until a valid edge is found (if one exists)
        while (edgeIterator.hasNext()) {
            TraversableGraphEdge<K> edge = edgeIterator.next();
            GraphNode<K> startNode = edge.getStartNode();
            GraphNode<K> endNode = edge.getEndNode();
            EdgeId<K> edgeId =
                new EdgeId<K>(startNode.getNodeId(), endNode.getNodeId());
            if (endNode.getNodeId().equals(sourceNode.getNodeId())
                || traversedEdges.contains(edgeId))
                continue; // Avoid loops
            traversedEdges.add(edgeId);
            traversedEdgeCount++;
            boolean alreadyTouched = touchedNodes.contains(endNode.getNodeId());
            touchedNodes.add(endNode.getNodeId());
            if (!alreadyTouched &&
                currentDepth < maxDepth) // TODO: Avoid re-adding already
                                         // traversed nodes
                neighborQueue.add(new NodeAndDepth<K>(edge.getEndNode(),
                                                      currentDepth));
            if (returnedNodes.contains(endNode.getNodeId())) // No duplicates
                continue;
            // Check the filter
            if (returnableFilter.accepts(edge.getStartNode().getNodeId(),
                                         edge.getEndNode().getNodeId())) {
                currentEdge = edge;
                return true; // Yup, we have an edge to return
            }
        }
        // If no edge was found and we've exhausted all immediate neighbors,
        // /dequeue a neighbor node and traverse it's out edges (thereby
        // descending on level deeper into the graph).
        if (!neighborQueue.isEmpty()) {
            NodeAndDepth<K> neighbor = neighborQueue.removeFirst();
            currentDepth = neighbor.depth + 1;
            this.edges = neighbor.node.traverseNeighbors(edgeType);
            return hasNext();
        }
        return false;
    }

    public GraphEdge<K> next() {
        if (sourceNode == null)
            return null;
        TraversableGraphEdge<K> next = currentEdge;
        currentEdge = null;
        returnedEdgeCount++;
        returnedNodes.add(next.getEndNode().getNodeId());
        return new GraphEdge<K>(next.getStartNode().getNodeId(),
                                next.getEndNode().getNodeId(), next.getType(),
                                next.getWeight());
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    // Util class used to keep track of the depth a node is at (relative to the
    // start node)
    private static class NodeAndDepth<K> {
        private final GraphNode<K> node;
        private final int depth;

        private NodeAndDepth(GraphNode<K> node, int depth) {
            this.node = node;
            this.depth = depth;
        }

        @Override
        public String toString() {
            return node.getNodeId() + " (" + depth + ")";
        }
    }

    // Used to avoid traversing the same edge twice
    private static class EdgeId<K> {
        private final NodeId<K> from, to;

        private EdgeId(NodeId<K> from, NodeId<K> to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + from.hashCode();
            result = prime * result + to.hashCode();
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EdgeId<K> other = (EdgeId<K>) obj;
            return other.from.equals(from) && other.to.equals(to);
        }

        @Override
        public String toString() {
            return from + " -> " + to;
        }
    }
}
