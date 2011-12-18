package tests.junit.db;

import recng.db.InMemoryKVStore;
import recng.db.KVStore;

public class TestInMemoryKVStore extends AbstractTestKVStore {

    @Override protected KVStore<String, String> getKVStore() {
        return new InMemoryKVStore<String, String>();
    }
}
