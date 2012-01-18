package recng.recommendations.graph;

import recng.graph.NodeID;
import recng.recommendations.domain.ProductNodeType;

/**
 * Represent the ID of a product node in a graph.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the product IDs.
 */
public class ProductID<T> extends NodeID<T> {

    public ProductID(T id) {
        super(id, ProductNodeType.getInstance());
    }
}
