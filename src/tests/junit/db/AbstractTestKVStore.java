package tests.junit.db;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.db.KVStore;

public abstract class AbstractTestKVStore {

    protected abstract KVStore<String, String> getKVStore();

    @Test public void testPutGetRemove() {
        KVStore<String, String> store = getKVStore();
        String key = "k";
        String value0 = "v0";
        String value1 = "v1";
        assertNull(store.get(key));
        store.put(key, value0);
        assertEquals(value0, store.get(key));
        store.put(key, value1);
        assertEquals(value1, store.get(key));
        store.remove(key);
        assertNull(store.get(key));
    }
}
