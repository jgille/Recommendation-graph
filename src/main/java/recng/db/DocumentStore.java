package recng.db;

import java.util.Map;

/**
 * A document store.
 * 
 * @author jon
 * 
 * @param <K>
 *            The generic type of the primary keys.
 */
public interface DocumentStore<K> extends KVStore<K, Map<String, Object>> {

}
