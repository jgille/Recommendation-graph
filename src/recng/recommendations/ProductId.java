package recng.recommendations;

import recng.graph.NodeId;

/**
 * Represent the ID of a product node in a graph.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the product IDs.
 */
public class ProductId<T> extends NodeId<T> {

    public ProductId(T id) {
        super(id, ProductNodeType.getInstance());
    }
}
