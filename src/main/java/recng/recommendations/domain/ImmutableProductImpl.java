package recng.recommendations.domain;

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

    private final Product product;

    public ImmutableProductImpl(Product product) {
        this.product = product;
    }

    @Override
    public String getID() {
        return product.getID();
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
    public Object getProperty(String key) {
        return product.getProperty(key);
    }

    @Override
    public List<Object> getRepeatedProperties(String key) {
        List<Object> properties = product.getRepeatedProperties(key);
        if (properties == null)
            return null;
        return new ArrayList<Object>(properties);
    }

    @Override
    public boolean containsProperty(String key) {
        return product.containsProperty(key);
    }

    @Override
    public String toString() {
        return product.toString();
    }
}
