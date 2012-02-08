package recng.recommendations.domain;

import recng.graph.NodeType;

/**
 * Describes all types of nodes used in recommendation graphs.
 * 
 * @author jon
 * 
 */
public enum RecommendationNodeType implements NodeType {

    PRODUCT, USER, SESSION;
}
