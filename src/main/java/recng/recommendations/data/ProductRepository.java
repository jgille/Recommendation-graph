package recng.recommendations.data;

import java.util.Map;

import recng.common.TableMetadata;

/**
 * An interface to a backend storage of product data.
 * 
 * @author jon
 * 
 */
public interface ProductRepository {

    Map<String, Object> getProductData(String id);

    TableMetadata getMetadata();
}
