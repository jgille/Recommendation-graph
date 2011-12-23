package recng.recommendations;

import java.util.ArrayList;
import java.util.List;

/**
 * An immutable representation of a product, delegating gets to a wrapped
 * {@link Product}.
 * 
 * NOTE: This instance is immutable in that it contains no methods that modifies
 * it. The wrapped product can still be modified though, which will take affect
 * here as well. If the get methods of this class returns mutable values, the
 * result of modifying these values is undefined.
 *
 * @author jon
 * 
 */
public class ImmutableProductImpl implements ImmutableProduct {

    private final String id;
    private final Product product;

    public ImmutableProductImpl(String id, Product product) {
        this.id = id;
        this.product = product;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isValid() {
        return product.isValid();
    }

    @Override
    public List<String> getCategories() {
        List<String> categories = product.getCategories();
        if (categories == null)
            return null;
        return new ArrayList<String>(categories);
    }

    @Override
    public <V> V getProperty(String key) {
        return product.getProperty(key);
    }

    @Override
    public <V> List<V> getRepeatedProperties(String key) {
        List<V> properties = product.getRepeatedProperties(key);
        if (properties == null)
            return null;
        return new ArrayList<V>(properties);
    }

    @Override
    public boolean containsProperty(String key) {
        return product.containsProperty(key);
    }
}
