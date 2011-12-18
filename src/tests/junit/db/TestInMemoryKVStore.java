package tests.junit.db;

import rectest.db.KVStore;
import rectest.db.InMemoryKVStore;

public class TestInMemoryKVStore extends AbstractTestKVStore {

    @Override protected KVStore<String, String> getKVStore() {
        return new InMemoryKVStore<String, String>();
    }
}
