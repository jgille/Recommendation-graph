package recng.db;

import java.io.IOException;
import java.util.Map;

import recng.common.BinPropertyContainer;
import recng.common.PropertyContainer;
import recng.common.PropertyContainerFactory;
import recng.common.TableMetadata;
import recng.common.io.CSVDescriptor;
import recng.common.io.CSVDialect;
import recng.common.io.CSVPropertyCursor;
import recng.common.io.CSVUtils;

public class InMemoryDocumentStore implements EmbeddedDocumentStore<String> {

    private final TableMetadata metadata;
    private final KVStore<String, PropertyContainer> storage;
    private String primaryKey;

    public InMemoryDocumentStore(TableMetadata metadata) {
        this.metadata = metadata;
        this.storage = new InMemoryKVStore<String, PropertyContainer>();
    }

    @Override
    public int importCSV(String file, CSVDialect dialect) throws IOException {
        CSVDescriptor descriptor = new CSVDescriptor();
        descriptor.setGzipped(file.endsWith(".gz")).setMetadata(metadata);
        PropertyContainerFactory factory = new BinPropertyContainer.Factory();
        CSVPropertyCursor cursor =
            CSVUtils.readAndParse(file, descriptor, factory);
        int added = 0;
        try {
            PropertyContainer props;
            while ((props = cursor.nextRow()) != null) {
                storage.put(getID(props.getProperty(primaryKey)), props);
                added++;
                if (added % 25000 == 0)
                    System.out.println(String
                        .format("Added %s documents..", added));
            }
        } finally {
            cursor.close();
        }
        return added;
    }

    @Override
    public void init(Map<String, String> properties) {
        this.primaryKey = properties.get("primary_key");
        storage.init(properties);
    }

    @Override
    public Map<String, Object> get(String key) {
        PropertyContainer props = storage.get(key);
        if (props != null)
            return props.asMap();
        return null;
    }

    @Override
    public void put(String key, Map<String, Object> properties) {
        BinPropertyContainer container =
            new BinPropertyContainer.Factory(false).create(metadata);
        container.mergeWith(properties);
        storage.put(key, container);
    }

    @Override
    public Map<String, Object> remove(String key) {
        PropertyContainer props = storage.remove(key);
        if (props != null)
            return props.asMap();
        return null;
    }

    private String getID(Object oid) {
        if (!(oid instanceof String))
            throw new IllegalArgumentException("IDs must be Strings: " + oid);
        return (String) oid;
    }
}
