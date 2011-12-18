package recng.common.filter;

import java.util.Set;

import recng.recommendations.Product;

public interface ProductFilter<K> {

    boolean accepts(Product<K> product);

    Set<String> getFilterProperties();
}
