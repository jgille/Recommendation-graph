package recng.recommendations;

import java.util.ArrayList;
import java.util.List;

import recng.graph.NodeID;
import recng.graph.NodeIDProcedure;
import recng.profiling.ProfilerEntry;
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.filter.ProductFilter;

/**
 * Procedure used to iterate immediate neighbors for a product node.
 * Neighbors are stored if matching the provided query, and may be received
 * with {@link GetProcuctNeighborsProcedure#getProducts()}.
 *
 * @author jon
 *
 */
class GetProcuctNeighborsProcedure<T> implements NodeIDProcedure<T> {

    private final ImmutableProductRepository<T> productRepo;
    private final List<ImmutableProduct> neighbors;
    private final ProductFilter filter;
    private final ProfilerEntry profilerEntry;
    private final int limit;
    private final int maxTraversed;
    private int traversed = 0;

    public GetProcuctNeighborsProcedure(ImmutableProductRepository<T> productRepo,
                                        NodeID<T> pid, ProductQuery query,
                                        ProfilerEntry profilerEntry) {
        this.productRepo = productRepo;
        this.neighbors = new ArrayList<ImmutableProduct>(query.getLimit());
        this.limit = query.getLimit();
        this.maxTraversed = query.getMaxCursorSize();
        final ProductFilter pFilter = query.getFilter();
        this.filter = new ProductFilter() {
            @Override
            public boolean accepts(ImmutableProduct product) {
                return product != null &&
                    product.isValid() && pFilter.accepts(product);
            }
        };
        this.profilerEntry = profilerEntry;
    }

    @Override
    public boolean apply(NodeID<T> neighbor) {
        if (neighbors.size() >= limit)
            return false;
        if (traversed++ >= maxTraversed)
            return false;
        T neighborID = neighbor.getID();
        ImmutableProduct product = productRepo.getImmutableProduct(neighborID);
        if (filter.accepts(product))
            neighbors.add(product);
        return true;
    }

    public List<ImmutableProduct> getProducts() {
        profilerEntry.setProperty("nReturned", neighbors.size());
        profilerEntry.setProperty("nIterated", traversed);
        profilerEntry.finish();
        return neighbors;
    }
}