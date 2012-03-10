package recng.graph;

import java.util.Collections;
import java.util.Set;

/**
 * Metadata about the valid edge and node types for a graph.
 * 
 * @author jon
 */
public class GraphMetadataImpl implements GraphMetadata {

    private final Set<EdgeType> edgeTypes;
    private final Set<NodeType> nodeTypes;

    public GraphMetadataImpl(Set<NodeType> nodeTypes,
                             Set<EdgeType> edgeTypes) {
        this.nodeTypes = Collections.unmodifiableSet(nodeTypes);
        this.edgeTypes = Collections.unmodifiableSet(edgeTypes);
    }

    @Override
    public Set<EdgeType> getEdgeTypes() {
        return edgeTypes;
    }

    @Override
    public Set<NodeType> getNodeTypes() {
        return nodeTypes;
    }
}
