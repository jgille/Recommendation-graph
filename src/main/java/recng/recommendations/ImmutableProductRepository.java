package recng.recommendations;

import recng.recommendations.domain.ImmutableProduct;

/**
 * A repository of {@link ImmutableProduct}s.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the keys in this repo.
 */
public interface ImmutableProductRepository<T> {

    ImmutableProduct getImmutableProduct(T productID);
}