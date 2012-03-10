package tests.junit.db.h2;

import java.util.HashMap;
import java.util.Map;

import recng.common.TableMetadata;
import recng.db.h2.H2KVStore;
import tests.junit.db.AbstractTestEmbeddedDocumentStore;

/**
 * Tests for {@link H2KVStore}.
 * 
 * @author jon
 * 
 */
public class TestH2KVStore extends AbstractTestEmbeddedDocumentStore {

    @Override
    protected H2KVStore initStore(String tableName) {
        TableMetadata metadata = getTableMetadata();
        H2KVStore store = new H2KVStore(metadata);
        Map<String, String> config = new HashMap<String, String>();
        config.put("url", getTempFolder());
        config.put("table", tableName);
        config.put("user", "test");
        config.put("pwd", "");
        config.put("primary_key", ID.getFieldName());
        store.init(config);
        store.createTableIfNotExists();
        return store;
    }
}
