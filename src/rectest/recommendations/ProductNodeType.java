package rectest.recommendations;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import rectest.graph.EdgeType;
import rectest.graph.NodeType;
import rectest.graph.NodeTypeImpl;

public class ProductNodeType extends NodeTypeImpl implements
    NodeType {

    private static final List<EdgeType> EDGE_TYPES =
        new ArrayList<EdgeType>(EnumSet.allOf(RecommendationType.class));

    private static final ProductNodeType INSTANCE = new ProductNodeType();

    public static ProductNodeType getInstance() {
        return INSTANCE;
    }

    private ProductNodeType() {
        super("Product node", EDGE_TYPES);
    }
}