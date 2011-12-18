package recng.recommendations;

import java.util.Map;

import recng.common.FieldSet;
import recng.db.KVStore;

public class ProductMetadataImpl implements ProductMetadata {

    private final KVStore<String, Map<String, Object>> db;
    private final FieldSet fields;

    public ProductMetadataImpl(KVStore<String, Map<String, Object>> db,
                               FieldSet fields) {
        this.db = db;
        this.fields = fields;
    }

    public Map<String, Object> getProductMetadata(String productId) {
        return db.get(productId);
    }

    public FieldSet getProductFields() {
        return fields;
    }
}