package recng.recommendations;

import java.util.Map;

import recng.common.TableMetadata;
import recng.db.KVStore;

public class ProductDataImpl implements ProductData {

    private final KVStore<String, Map<String, Object>> db;
    private final TableMetadata fields;

    public ProductDataImpl(KVStore<String, Map<String, Object>> db,
                               TableMetadata fields) {
        this.db = db;
        this.fields = fields;
    }

    public Map<String, Object> getProductData(String productId) {
        return db.get(productId);
    }

    public TableMetadata getProductFields() {
        return fields;
    }
}
