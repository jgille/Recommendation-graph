package recng.recommendations.filter;

import java.util.Set;

import recng.recommendations.ImmutableProduct;

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

    /**
     * Gets the set of property names that this filter uses.
     */
    Set<String> getFilterProperties();
}
