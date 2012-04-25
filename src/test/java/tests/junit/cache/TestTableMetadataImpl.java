package tests.junit.cache;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.FieldType;
import recng.common.TableMetadata;
import recng.common.TableMetadataImpl;

/**
 * Tests for {@link recng.common.TableMetadataImpl}.
 * 
 * @author Jon Ivmark
 */
public class TestTableMetadataImpl {

    private static final FieldMetadata PRICE =
        FieldMetadataImpl.create("Price",
                                 FieldType.INT);
    private static final FieldMetadata ISBN =
        FieldMetadataImpl.create("ISBN",
                                 FieldType.STRING);

    @Test
    public void testGetFieldMetadata() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, ISBN));
        assertEquals(PRICE, fs.getFieldMetadata(PRICE.getFieldName()));
        assertEquals(ISBN, fs.getFieldMetadata(ISBN.getFieldName()));
    }

    @Test
    public void testContains() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, ISBN));
        assertTrue(fs.contains(PRICE.getFieldName()));
        assertFalse(fs.contains("foo"));
    }

    @Test
    public void testGetFields() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, ISBN));
        List<String> fields = fs.getFields();
        assertEquals(2, fields.size());
        assertTrue(fs.contains(PRICE.getFieldName()));
        assertTrue(fs.contains(ISBN.getFieldName()));
    }

    @Test
    public void testOrdinal() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, ISBN));
        assertEquals(0, fs.ordinal(PRICE.getFieldName()));
        assertEquals(1, fs.ordinal(ISBN.getFieldName()));
    }

    @Test
    public void testSize() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, ISBN));
        assertEquals(2, fs.size());
    }
}
