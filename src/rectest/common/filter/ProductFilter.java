package rectest.common.filter;

import java.util.Set;

import rectest.recommendations.Product;

public interface ProductFilter<K> {

    boolean accepts(Product<K> product);

    Set<String> getFilterProperties();
}
