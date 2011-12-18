package rectest.recommendations;

public interface KeyParser<K> {

    K parseKey(String id);

    String toString(K productId);
}
