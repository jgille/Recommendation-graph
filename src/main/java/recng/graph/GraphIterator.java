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
    private TraversableGraphEdge<T> nextEdge = null;
    // Queued nodes used when we want to traverse at a depth > 1
    private final LinkedList<NodeAndDepth<T>> neighborQueue =
        new LinkedList<NodeAndDepth<T>>();
    // Avoid duplicates among returned end nodes
    private final Set<NodeID<T>> returnedNodes = new HashSet<NodeID<T>>();
    // Used to avoid using the same start node twice
    private final Set<NodeID<T>> startNodes = new HashSet<NodeID<T>>();


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

    int getReturnedEdgeCount() {
        return returnedEdgeCount;
    }

    int getTraversedEdgeCount() {
        return traversedEdgeCount;
    }

    int currentDepth() {
        return currentDepth;
    }

    public boolean hasNext() {
        if (sourceNode == null)
            return false;
        return hasNext(edges);
    }

    private boolean hasNext(Iterator<TraversableGraphEdge<T>> edgeIterator) {
        if (nextEdge != null)
            return true;
        if (returnedEdgeCount >= maxReturnedEdges)
            return false; // We've filled the quota
        if (currentDepth > maxDepth)
            return false; // We've traversed too deeply

        // Iterate immediate out edges until a valid edge is found (if one exists)
        while (edgeIterator.hasNext()) {
            if (traversedEdgeCount++ >= maxTraversedEdges)
                return false; // We've traversed too many edges

            TraversableGraphEdge<T> edge = edgeIterator.next();
            GraphNode<T> startNode = edge.getStartNode();
            GraphNode<T> endNode = edge.getEndNode();

            if (startNode == null || endNode == null)
                throw new IllegalStateException("Null node for edge: " + edge);
            if (startNode.getNodeId().equals(endNode.getNodeId()))
                throw new IllegalStateException("Illegal edge: " + edge);
            if (endNode.getNodeId().equals(sourceNode.getNodeId()))
                continue; // Avoid loops
            if (returnedNodes.contains(endNode.getNodeId()))
                continue; // Avoid duplicates
            if (!startNodes.contains(endNode.getNodeId())) {
                // Add the end node as a potential start node for furter
                // traversal
                neighborQueue.add(new NodeAndDepth<T>(endNode, currentDepth));
                startNodes.add(endNode.getNodeId());
            }
            // Check the filter
            if (returnableFilter.accepts(startNode.getNodeId(),
                                         endNode.getNodeId())) {
                nextEdge = edge;
                return true; // Yup, we have an edge to return
            }
        }
        // If no edge was found and we've exhausted all immediate neighbors,
        // dequeue a neighbor node and traverse it's out edges (thereby
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
        TraversableGraphEdge<T> next = nextEdge;
        nextEdge = null;
        returnedEdgeCount++;
        returnedNodes.add(next.getEndNode().getNodeId());
        return new GraphEdge<T>(next.getStartNode().getNodeId(),
                                next.getEndNode().getNodeId(), next.getType(),
                                next.getWeight());
    }

    /**
     * Returns the next edge without iterating passed it, i.e. the next call to
     * {@link GraphIterator#peekNext()} or {@link GraphIterator#next()} will
     * return the same edge again.
     */
    public GraphEdge<T> peekNext() {
        TraversableGraphEdge<T> next = nextEdge;
        return new GraphEdge<T>(next.getStartNode().getNodeId(),
                                next.getEndNode().getNodeId(), next.getType(),
                                next.getWeight());
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return String.format("start: %s, next: %s, returned: %s," +
            "traversed: %s, depth: %s",
                             sourceNode, nextEdge, returnedEdgeCount,
                             traversedEdgeCount, currentDepth);
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
}
