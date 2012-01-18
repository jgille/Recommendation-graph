package recng.recommendations.data;

import java.util.Map;

import recng.common.TableMetadata;
import recng.db.KVStore;

/**
 * An implementation of a data store that is backed by a {@link KVStore}
 * containing the product properties.
 *
 * @author jon
 *
 */
public class DataStoreImpl implements DataStore {

    private final KVStore<String, Map<String, Object>> db;
    private final TableMetadata fields;

    public DataStoreImpl(KVStore<String, Map<String, Object>> db,
                         TableMetadata fields) {
        this.db = db;
        this.fields = fields;
    }

    public Map<String, Object> getData(String productId) {
        return db.get(productId);
    }

    public TableMetadata getMetadata() {
        return fields;
    }
}
