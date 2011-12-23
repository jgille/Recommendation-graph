package recng.recommendations;

/**
 * Cached product data.
 *
 * @author jon
 *
 * @param <K>
 *            The generic type of the keys in this cache
 */
public interface ProductCache<K> {

    /**
     * Gets cached properties for a product. Node that the returned instance may
     * only contain a subset of the available product properties.
     */
    Product<K> getProduct(K productId);

    /**
     * Caches product properties.
     */
    void cacheProduct(Product<K> product);

    /**
     * Clears the cache.
     */
    void clearCache();

    /**
     * Removed a product from the cache.
     */
    void remove(K productId);
}
