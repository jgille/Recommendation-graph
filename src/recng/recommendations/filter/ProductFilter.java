package recng.recommendations.filter;

import recng.recommendations.domain.ImmutableProduct;

/**
 * A product filter used to filter product from a recommnedation.
 *
 * @author jon
 *
 * @param <K>
 *            The generic type of the product IDs.
 */
public interface ProductFilter {

    /**
     * Returns true if the provided product is accepted according to this
     * filter.
     */
    boolean accepts(ImmutableProduct product);
}
