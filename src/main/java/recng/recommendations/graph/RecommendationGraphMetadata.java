package recng.recommendations.graph;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import recng.graph.EdgeType;
import recng.graph.GraphMetadata;
import recng.graph.GraphMetadataImpl;
import recng.graph.NodeType;
import recng.recommendations.domain.RecommendationNodeType;

/**
 * Metadata for a recommendation graph.
 * 
 * @author jon
 * 
 */
public class RecommendationGraphMetadata extends GraphMetadataImpl implements
    GraphMetadata {

    private static final Set<NodeType> NODE_TYPES =
        new HashSet<NodeType>(EnumSet.allOf(RecommendationNodeType.class));

    private static final Set<EdgeType> EDGE_TYPES =
        new LinkedHashSet<EdgeType>(EnumSet.allOf(RecommendationEdgeType.class));

    private static final GraphMetadata INSTANCE =
        new RecommendationGraphMetadata(NODE_TYPES, EDGE_TYPES);

    private RecommendationGraphMetadata(Set<NodeType> nodeTypes,
                                        Set<EdgeType> edgeTypes) {
        super(nodeTypes, edgeTypes);
    }

    public static GraphMetadata getInstance() {
        return INSTANCE;
    }
}
