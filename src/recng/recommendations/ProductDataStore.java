package recng.recommendations;

import java.util.Map;

import recng.common.TableMetadata;

/**
 * An interface to a backend storage of product data.
 * 
 * @author jon
 * 
 */
public interface ProductDataStore {

    Map<String, Object> getProductData(String productId);

    TableMetadata getProductFields();
}
