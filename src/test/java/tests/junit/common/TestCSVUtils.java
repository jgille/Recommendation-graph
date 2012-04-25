package tests.junit.common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import au.com.bytecode.opencsv.CSVWriter;

import recng.common.BinPropertyContainer;
import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.FieldType;
import recng.common.PropertyContainer;
import recng.common.PropertyContainerFactory;
import recng.common.TableMetadata;
import recng.common.TableMetadataImpl;
import recng.common.io.CSVCursor;
import recng.common.io.CSVDescriptor;
import recng.common.io.CSVDialect;
import recng.common.io.CSVPropertyCursor;
import recng.common.io.CSVUtils;

/**
 * Simple tests for {@link CSVUtils}.
 * 
 * @author jon
 * 
 */
public class TestCSVUtils {

    private static final List<String[]> CSV_CONTENT =
        Arrays.asList(new String[] { "1A", "1.0", "1" },
                      new String[] { "2A", "2.0" },
                      new String[] { "3A", "3.0", "" });

    private static final List<String> COLUMNS = Arrays.asList("C0", "C1", "C2");

    private static final int DEFAULT_INT = 10;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private String createTempFolder() throws IOException {
        return tempFolder.newFolder().getAbsolutePath();
    }

    private String createCSV(List<String[]> rows) throws IOException {
        String file = createTempFolder() + "test.csv";
        CSVDialect dialect = new CSVDialect();
        CSVWriter csvWriter = null;
        try {
            Writer writer = new FileWriter(file);
            csvWriter =
                new CSVWriter(writer, dialect.getSeparator(),
                              dialect.getQuoteChar(), dialect.getEscapeChar());
            csvWriter.writeAll(rows);
        } finally {
            csvWriter.close();
        }
        return file;
    }

    @Test
    public void testReadAll() throws IOException {
        String csv = createCSV(CSV_CONTENT);
        List<String[]> rows = CSVUtils.readAll(csv, new CSVDescriptor());
        assertSameContent(CSV_CONTENT, rows);
    }

    @Test
    public void testRead() throws IOException {
        String csv = createCSV(CSV_CONTENT);
        List<String[]> rows = new ArrayList<String[]>();
        CSVCursor<String[]> cursor = CSVUtils.read(csv, new CSVDescriptor());
        try {
            String[] row;
            while ((row = cursor.nextRow()) != null)
                rows.add(row);
        } finally {
            cursor.close();
        }
        assertSameContent(CSV_CONTENT, rows);
    }

    @Test
    public void testReadAndParseAll() throws IOException {
        String csv = createCSV(CSV_CONTENT);
        PropertyContainerFactory factory = new BinPropertyContainer.Factory();
        CSVDescriptor descriptor = new CSVDescriptor();
        TableMetadata metadata = getMetadata();
        descriptor.setMetadata(metadata);
        List<PropertyContainer> entries =
            CSVUtils.readAndParseAll(csv, descriptor, factory);
        List<Map<String, Object>> expected = getExpectedProperties();
        List<Map<String, Object>> actual = new ArrayList<Map<String, Object>>();
        for (PropertyContainer props : entries)
            actual.add(props.asMap());
        assertSameProperties(expected, actual);
    }

    @Test
    public void testReadAndParse() throws IOException {
        String csv = createCSV(CSV_CONTENT);
        PropertyContainerFactory factory = new BinPropertyContainer.Factory();
        CSVDescriptor descriptor = new CSVDescriptor();
        TableMetadata metadata = getMetadata();
        descriptor.setMetadata(metadata);
        List<Map<String, Object>> actual = new ArrayList<Map<String, Object>>();
        CSVPropertyCursor cursor =
            CSVUtils.readAndParse(csv, descriptor, factory);
        try {
            PropertyContainer props;
            while ((props = cursor.nextRow()) != null)
                actual.add(props.asMap());
        } finally {
            cursor.close();
        }
        List<Map<String, Object>> expected = getExpectedProperties();
        assertSameProperties(expected, actual);
    }

    private static TableMetadata getMetadata() {
        List<FieldMetadata> fields = new ArrayList<FieldMetadata>();
        fields.add(FieldMetadataImpl.create(COLUMNS.get(0),
                                            FieldType.STRING));
        fields.add(FieldMetadataImpl.create(COLUMNS.get(1),
                                            FieldType.DOUBLE));
        fields.add(new FieldMetadataImpl.Builder(COLUMNS.get(2),
                                                 FieldType.INT)
            .setDefaultValue(DEFAULT_INT).build());
        return new TableMetadataImpl(fields);
    }

    private static List<Map<String, Object>> getExpectedProperties() {
        List<Map<String, Object>> expected =
            new ArrayList<Map<String, Object>>();
        for (String[] row : CSV_CONTENT)
            expected.add(toMap(row));
        return expected;
    }

    private static Map<String, Object> toMap(String[] row) {
        Map<String, Object> map = new HashMap<String, Object>();
        int i = 0;
        for (String fieldValue : row) {
            String column = COLUMNS.get(i);
            switch (i) {
            case 0:
                map.put(column, fieldValue);
                break;
            case 1:
                map.put(column, Double.valueOf(fieldValue));
                break;
            case 2:
                if (!fieldValue.isEmpty())
                    map.put(column, Integer.valueOf(fieldValue));
                else
                    map.put(column, DEFAULT_INT);
                break;
            }
            i++;
        }
        return map;
    }

    private static void assertSameContent(List<String[]> expected,
                                          List<String[]> actual) {
        Assert.assertNotNull("Got null content", actual);
        Assert.assertEquals("Wrong number of rows", expected.size(),
                            actual.size());
        int i = 0;
        for (String[] expectedRow : expected) {
            String[] actualRow = actual.get(i++);
            Assert.assertArrayEquals("Unexpected row on line: " + (i + 1),
                                     expectedRow, actualRow);
        }
    }

    private static void
        assertSameProperties(List<Map<String, Object>> expected,
                             List<Map<String, Object>> actual) {
        Assert.assertNotNull("Got null content", actual);
        Assert.assertEquals("Wrong number of rows", expected.size(),
                            actual.size());
        int i = 0;
        for (Map<String, Object> expectedProps : expected) {
            Map<String, Object> actualProps = actual.get(i++);
            Assert.assertEquals("Wrong properties on line: " + (i + 1),
                                expectedProps, actualProps);
        }
    }
}
