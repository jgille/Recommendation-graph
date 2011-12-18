package rectest.graph;

import java.util.Iterator;

interface GraphNode<K> {
    Iterator<TraversableGraphEdge<K>> traverseNeighbors(EdgeType edgeType);

    NodeId<K> getNodeId();
}
