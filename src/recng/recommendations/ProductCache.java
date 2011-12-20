package recng.recommendations;


public interface ProductCache<K> {

    Product<K> getProduct(K productId);

    void cacheProduct(Product<K> product);

    void clearCache();

    void remove(K productId);
}
