package tests.junit.db.orientdb;

import java.util.HashMap;
import java.util.Map;

import recng.db.orientdb.OrientKVStore;
import tests.junit.db.AbstractTestEmbeddedDocumentStore;

/**
 * Tests for {@link OrientKVStore}.
 * 
 * @author jon
 * 
 */
public class TestOrientKVStore extends AbstractTestEmbeddedDocumentStore {

    @Override
    protected OrientKVStore initStore(String className) {
        OrientKVStore store = new OrientKVStore(getTableMetadata());
        Map<String, String> config = new HashMap<String, String>();
        config.put("url", getTempFolder());
        config.put("oClass", className);
        config.put("user", "admin");
        config.put("pwd", "admin");
        config.put("primary_key", ID.getFieldName());
        store.init(config);
        store.setupSchema();
        return store;
    }
}
