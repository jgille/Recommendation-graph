package rectest.recommendations;


public interface ProductMetadataCache<K> {

    Product<K> getProduct(K productId);

    void cacheProduct(Product<K> product);

    void clearCache();

    void remove(K productId);
}
