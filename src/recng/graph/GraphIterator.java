package recng.graph;

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
 * @param <T>
 *            The type of the node keys in the graph.
 */
public class GraphIterator<T> implements Iterator<GraphEdge<T>> {

    // Start node
    private final GraphNode<T> sourceNode;
    // Current out edges
    private Iterator<TraversableGraphEdge<T>> edges;
    // The type of edges to traverse
    private final EdgeType edgeType;
    // A filter used to decide if and edge should be returned or not
    private final EdgeFilter<T> returnableFilter;
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
    private TraversableGraphEdge<T> currentEdge = null;
    // Queued nodes used when we want to traverse at a depth > 1
    private final LinkedList<NodeAndDepth<T>> neighborQueue =
        new LinkedList<NodeAndDepth<T>>();
    // Avoid loops
    private final Set<EdgeId<T>> traversedEdges = new HashSet<EdgeId<T>>();
    // Avoid duplicates among returned end nodes
    private final Set<NodeID<T>> returnedNodes = new HashSet<NodeID<T>>();
    // Used to avoid using the same start node twice
    private final Set<NodeID<T>> visitedNodes = new HashSet<NodeID<T>>();

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
    GraphIterator(GraphNode<T> sourceNode, EdgeType edgeType,
                  EdgeFilter<T> returnableFilter, int maxDepth,
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

    private boolean hasNext(Iterator<TraversableGraphEdge<T>> edgeIterator) {
        if (returnedEdgeCount >= maxReturnedEdges)
            return false; // We've filled the quota
        if (traversedEdgeCount >= maxTraversedEdges)
            return false; // We've traversed too many edges
        if (currentDepth > maxDepth)
            return false; // We've traversed too deeply

        // Iterate immediate out edges until a valid edge is found (if one exists)
        while (edgeIterator.hasNext()) {
            TraversableGraphEdge<T> edge = edgeIterator.next();
            GraphNode<T> startNode = edge.getStartNode();
            GraphNode<T> endNode = edge.getEndNode();
            if (startNode == null || endNode == null)
                continue;
            EdgeId<T> edgeId =
                new EdgeId<T>(startNode.getNodeId(), endNode.getNodeId());
            if (endNode.getNodeId().equals(sourceNode.getNodeId())
                || traversedEdges.contains(edgeId))
                continue; // Avoid loops
            traversedEdges.add(edgeId);
            traversedEdgeCount++;

            boolean alreadyVisited = visitedNodes.contains(endNode.getNodeId());
            visitedNodes.add(endNode.getNodeId());
            if (!alreadyVisited && currentDepth < maxDepth)
                neighborQueue.add(new NodeAndDepth<T>(endNode, currentDepth));

            if (returnedNodes.contains(endNode.getNodeId())) // No duplicates
                continue;

            // Check the filter
            if (returnableFilter.accepts(startNode.getNodeId(),
                                         endNode.getNodeId())) {
                currentEdge = edge;
                return true; // Yup, we have an edge to return
            }
        }
        // If no edge was found and we've exhausted all immediate neighbors,
        // /dequeue a neighbor node and traverse it's out edges (thereby
        // descending on level deeper into the graph).
        if (!neighborQueue.isEmpty()) {
            NodeAndDepth<T> neighbor = neighborQueue.removeFirst();
            currentDepth = neighbor.depth + 1;
            this.edges = neighbor.node.traverseNeighbors(edgeType);
            return hasNext();
        }
        return false;
    }

    public GraphEdge<T> next() {
        if (sourceNode == null)
            return null;
        TraversableGraphEdge<T> next = currentEdge;
        currentEdge = null;
        returnedEdgeCount++;
        returnedNodes.add(next.getEndNode().getNodeId());
        return new GraphEdge<T>(next.getStartNode().getNodeId(),
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
    // TODO: Use EdgeImpl instead
    private static class EdgeId<K> {
        private final NodeID<K> from, to;

        private EdgeId(NodeID<K> from, NodeID<K> to) {
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
