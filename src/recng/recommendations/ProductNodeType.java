package recng.recommendations;

import recng.graph.NodeType;
import recng.graph.NodeTypeImpl;

/**
 * A product node in a graph.
 *
 * TODO: This should probaly be contained in an enum.
 *
 * @author jon
 *
 */
public class ProductNodeType extends NodeTypeImpl implements
    NodeType {

    private static final ProductNodeType INSTANCE = new ProductNodeType();

    public static ProductNodeType getInstance() {
        return INSTANCE;
    }

    // This is a singleton
    private ProductNodeType() {
        super("Product node", 0);
    }
}