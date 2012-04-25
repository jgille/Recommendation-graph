package tests.junit.db;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import au.com.bytecode.opencsv.CSVWriter;

import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.FieldType;
import recng.common.TableMetadata;
import recng.common.TableMetadataImpl;
import recng.common.io.CSVDialect;
import recng.db.EmbeddedDocumentStore;
import static org.junit.Assert.*;

/**
 * Base test class for {@link EmbeddedDocumentStore}s.
 * 
 * @author jon
 * 
 */
public abstract class AbstractTestEmbeddedDocumentStore {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected static final FieldMetadata ID =
        FieldMetadataImpl.create("__id", FieldType.STRING);
    private static final FieldMetadata NAME =
        FieldMetadataImpl.create("Name", FieldType.STRING);
    private static final FieldMetadata PRICE =
        FieldMetadataImpl.create("Price", FieldType.DOUBLE);
    private static final FieldMetadata COUNT =
        FieldMetadataImpl.create("Count", FieldType.INT);
    private static final FieldMetadata VALID =
        FieldMetadataImpl.create("Valid", FieldType.BOOLEAN);
    private static final FieldMetadata CATEGORIES =
        new FieldMetadataImpl.Builder("Categories", FieldType.STRING)
            .setRepeated(true).build();

    protected String getTempFolder() {
        try {
            return tempFolder.newFolder().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testPutGetRemove() {
        EmbeddedDocumentStore<String> store = initStore("testPutGetRemove");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ID.getFieldName(), "key");
        map.put(NAME.getFieldName(), "Name");
        map.put(COUNT.getFieldName(), 10);
        map.put(PRICE.getFieldName(), 5.5d);
        map.put(VALID.getFieldName(), true);
        map.put(CATEGORIES.getFieldName(), Arrays.asList("cat1", "cat2"));
        store.put("key", map);
        Map<String, Object> actual = store.get("key");
        assertEquals(map, actual);
        store.remove("key");
        actual = store.get("key");
        assertNull(actual);
    }

    @Test
    public void testImport() throws SQLException, IOException {
        EmbeddedDocumentStore<String> store = initStore("testImport");
        String csvFile = getTempFolder() + "/test.csv";
        CSVDialect dialect = new CSVDialect();
        CSVWriter csvWriter = null;
        try {
            Writer writer = new FileWriter(csvFile);
            csvWriter =
                new CSVWriter(writer, dialect.getSeparator(),
                              dialect.getQuoteChar(), dialect.getEscapeChar());
            csvWriter.writeNext(new String[] { "key1", "Name", "5.5", "10",
                    "true", "cat1" });
            csvWriter.writeNext(new String[] { "key2", "Name2", "1.5", "100",
                    "false", "cat2" });

        } finally {
            if (csvWriter != null)
                csvWriter.close();
        }
        store.importCSV(csvFile, dialect);
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put(ID.getFieldName(), "key1");
        map1.put(NAME.getFieldName(), "Name");
        map1.put(COUNT.getFieldName(), 10);
        map1.put(PRICE.getFieldName(), 5.5d);
        map1.put(VALID.getFieldName(), true);
        map1.put(CATEGORIES.getFieldName(), Arrays.asList("cat1"));
        Map<String, Object> actual1 = store.get("key1");
        assertEquals(map1, actual1);

        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put(ID.getFieldName(), "key2");
        map2.put(NAME.getFieldName(), "Name2");
        map2.put(COUNT.getFieldName(), 100);
        map2.put(PRICE.getFieldName(), 1.5d);
        map2.put(VALID.getFieldName(), false);
        map2.put(CATEGORIES.getFieldName(), Arrays.asList("cat2"));
        Map<String, Object> actual2 = store.get("key2");
        assertEquals(map2, actual2);

    }

    protected abstract EmbeddedDocumentStore<String> initStore(String name);

    protected TableMetadata getTableMetadata() {
        List<FieldMetadata> fields = new ArrayList<FieldMetadata>();
        fields.add(ID);
        fields.add(NAME);
        fields.add(PRICE);
        fields.add(COUNT);
        fields.add(VALID);
        fields.add(CATEGORIES);
        return new TableMetadataImpl(fields);
    }
}
