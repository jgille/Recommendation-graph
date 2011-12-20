package tests.junit.cache;

import java.util.Set;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.TableMetadata;
import recng.common.TableMetadataImpl;
import recng.common.Marshallers;

/**
 * Tests for {@link recng.common.TableMetadataImpl}.
 *
 * @author Jon Ivmark
 */
public class TestFieldSetImpl {

    private static final FieldMetadata<Integer> PRICE =
        new FieldMetadataImpl<Integer>("price",
                                       Marshallers.INTEGER_MARSHALLER,
                                       FieldMetadata.Type.INTEGER);
    private static final FieldMetadata<String> ISBN =
        new FieldMetadataImpl<String>("ISBN",
                                      Marshallers.STRING_MARSHALLER,
                                      FieldMetadata.Type.STRING);

    @Test public void testBuilder() {
        TableMetadataImpl.Builder builder = new TableMetadataImpl.Builder();
        builder.add(PRICE).build();
        boolean exception = false;
        try {
            builder.add(ISBN);
        } catch (IllegalStateException e) {
            exception = true;
        }
        assertTrue(exception);

        builder = new TableMetadataImpl.Builder();
        exception = false;
        try {
            builder.add(PRICE).add(PRICE);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test public void testGetFieldMetadata() {
        TableMetadata fs = new TableMetadataImpl.Builder().add(PRICE).add(ISBN).build();
        assertEquals(PRICE, fs.getFieldMetadata(PRICE.getFieldName()));
        assertEquals(ISBN, fs.getFieldMetadata(ISBN.getFieldName()));
        boolean exception = false;
        try {
            fs.getFieldMetadata("foo");
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test public void testContains() {
        TableMetadata fs = new TableMetadataImpl.Builder().add(PRICE).build();
        assertTrue(fs.contains(PRICE.getFieldName()));
        assertFalse(fs.contains("foo"));
    }

    @Test public void testTypeOf() {
        TableMetadata fs = new TableMetadataImpl.Builder().add(PRICE).add(ISBN).build();
        assertEquals(FieldMetadata.Type.INTEGER, fs.typeOf(PRICE.getFieldName()));
        assertEquals(FieldMetadata.Type.STRING, fs.typeOf(ISBN.getFieldName()));
    }

    @Test public void testGetFields() {
        TableMetadata fs = new TableMetadataImpl.Builder().add(PRICE).add(ISBN).build();
        Set<String> fields = fs.getFields();
        assertEquals(2, fields.size());
        assertTrue(fs.contains(PRICE.getFieldName()));
        assertTrue(fs.contains(ISBN.getFieldName()));
    }

    @Test public void testOrdinal() {
        TableMetadata fs = new TableMetadataImpl.Builder().add(PRICE).add(ISBN).build();
        assertEquals(0, fs.ordinal(PRICE.getFieldName()));
        assertEquals(1, fs.ordinal(ISBN.getFieldName()));
    }

    @Test public void testSize() {
        TableMetadata fs = new TableMetadataImpl.Builder().add(PRICE).add(ISBN).build();
        assertEquals(2, fs.size());
   }
}
