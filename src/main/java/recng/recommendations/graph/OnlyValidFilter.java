package recng.recommendations.graph;

import recng.graph.EdgeFilter;
import recng.graph.NodeID;
import recng.recommendations.ImmutableProductRepository;
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.filter.ProductFilter;

/**
 * A filter that requires valid products in combination with a provided filter.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the node ids.
 */
public class OnlyValidFilter<T> implements EdgeFilter<T> {

    private final ProductFilter pFilter;
    private final ImmutableProductRepository<T> productRepo;

    public OnlyValidFilter(ProductFilter pFilter, ImmutableProductRepository<T> productRepo) {
        this.pFilter = pFilter;
        this.productRepo = productRepo;
    }

    public boolean accepts(NodeID<T> start, NodeID<T> end) {
        ImmutableProduct product = productRepo.getImmutableProduct(end.getID());
        boolean accepts = product != null &&
            product.isValid() && pFilter.accepts(product);
        return accepts;
    }
}