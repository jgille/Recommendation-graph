package recng.recommendations.data;

import java.util.Map;

import recng.common.TableMetadata;

/**
 * An interface to a backend storage of product data etc.
 * 
 * @author jon
 * 
 */
public interface DataStore {

    Map<String, Object> getData(String id);

    TableMetadata getMetadata();
}
