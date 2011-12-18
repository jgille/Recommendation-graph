package tests.junit.cache;

import java.util.Set;

import static org.junit.Assert.*;
import org.junit.Test;

import rectest.common.FieldMetadata;
import rectest.common.FieldMetadataImpl;
import rectest.common.FieldSet;
import rectest.common.FieldSetImpl;
import rectest.common.Marshallers;

/**
 * Tests for {@link rectest.common.FieldSetImpl}.
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
        FieldSetImpl.Builder builder = new FieldSetImpl.Builder();
        builder.add(PRICE).build();
        boolean exception = false;
        try {
            builder.add(ISBN);
        } catch (IllegalStateException e) {
            exception = true;
        }
        assertTrue(exception);

        builder = new FieldSetImpl.Builder();
        exception = false;
        try {
            builder.add(PRICE).add(PRICE);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test public void testGetFieldMetadata() {
        FieldSet fs = new FieldSetImpl.Builder().add(PRICE).add(ISBN).build();
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
        FieldSet fs = new FieldSetImpl.Builder().add(PRICE).build();
        assertTrue(fs.contains(PRICE.getFieldName()));
        assertFalse(fs.contains("foo"));
    }

    @Test public void testTypeOf() {
        FieldSet fs = new FieldSetImpl.Builder().add(PRICE).add(ISBN).build();
        assertEquals(FieldMetadata.Type.INTEGER, fs.typeOf(PRICE.getFieldName()));
        assertEquals(FieldMetadata.Type.STRING, fs.typeOf(ISBN.getFieldName()));
    }

    @Test public void testGetFields() {
        FieldSet fs = new FieldSetImpl.Builder().add(PRICE).add(ISBN).build();
        Set<String> fields = fs.getFields();
        assertEquals(2, fields.size());
        assertTrue(fs.contains(PRICE.getFieldName()));
        assertTrue(fs.contains(ISBN.getFieldName()));
    }

    @Test public void testOrdinal() {
        FieldSet fs = new FieldSetImpl.Builder().add(PRICE).add(ISBN).build();
        assertEquals(0, fs.ordinal(PRICE.getFieldName()));
        assertEquals(1, fs.ordinal(ISBN.getFieldName()));
    }

    @Test public void testSize() {
        FieldSet fs = new FieldSetImpl.Builder().add(PRICE).add(ISBN).build();
        assertEquals(2, fs.size());
   }
}
