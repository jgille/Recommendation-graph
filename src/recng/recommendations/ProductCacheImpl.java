package recng.recommendations;

import recng.cache.Cache;

public class ProductCacheImpl<K> implements ProductCache<K> {

    private final Cache<K, Product<K>> cache;

    public ProductCacheImpl(Cache<K, Product<K>> cache) {
        this.cache = cache;
    }

    public Product<K> getProduct(K productId) {
        return cache.get(productId);
    }

    public void cacheProduct(Product<K> product) {
        cache.cache(product.getId(), product);
    }

    public void clearCache() {
        cache.clear();
    }

    public void remove(K productId) {
        cache.evict(productId);
    }
}
