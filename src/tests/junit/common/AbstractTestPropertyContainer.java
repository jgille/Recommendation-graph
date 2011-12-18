package tests.junit.common;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.FieldSet;
import recng.common.FieldSetImpl;
import recng.common.Marshallers;
import recng.common.PropertyContainer;

public abstract class AbstractTestPropertyContainer {

    protected static final FieldMetadata<Integer> PRICE =
        new FieldMetadataImpl<Integer>("Price",
                                       Marshallers.INTEGER_MARSHALLER,
                                       FieldMetadata.Type.INTEGER);
    protected static final FieldMetadata<String> ISBN =
        new FieldMetadataImpl<String>("ISBN",
                                      Marshallers.STRING_MARSHALLER,
                                      FieldMetadata.Type.STRING);

    protected static final FieldMetadata<Long> RELEASE_DATE =
        new FieldMetadataImpl<Long>("ReleaseDate",
                                    Marshallers.LONG_MARSHALLER,
                                    FieldMetadata.Type.LONG);

    protected static final FieldMetadata<String> CATEGORIES =
        new FieldMetadataImpl<String>("Categories",
                                      Marshallers.STRING_MARSHALLER,
                                      FieldMetadata.Type.STRING, true);

    protected abstract PropertyContainer<String> getPropertyContainer(FieldSet fs);

    @Test public void testGetSetContainsProperty() {
        FieldSet fs = new FieldSetImpl.Builder().add(PRICE).add(ISBN).add(RELEASE_DATE).build();
        PropertyContainer<String> properties = getPropertyContainer(fs);

        assertFalse(properties.containsProperty(PRICE.getFieldName()));
        assertNull(properties.getProperty(PRICE.getFieldName()));
        assertFalse(properties.containsProperty(ISBN.getFieldName()));
        assertNull(properties.getProperty(ISBN.getFieldName()));

        properties.setProperty(PRICE.getFieldName(), 100);
        properties.setProperty(ISBN.getFieldName(), "123");

        assertTrue(properties.containsProperty(PRICE.getFieldName()));
        assertEquals(100, properties.getProperty(PRICE.getFieldName()));
        assertTrue(properties.containsProperty(ISBN.getFieldName()));
        assertEquals("123", properties.getProperty(ISBN.getFieldName()));

        properties.setProperty(RELEASE_DATE.getFieldName(), 7777l);
        assertTrue(properties.containsProperty(RELEASE_DATE.getFieldName()));
        assertEquals(7777l, properties.getProperty(RELEASE_DATE.getFieldName()));
        assertTrue(properties.containsProperty(PRICE.getFieldName()));
        assertEquals(100, properties.getProperty(PRICE.getFieldName()));
        assertTrue(properties.containsProperty(ISBN.getFieldName()));
        assertEquals("123", properties.getProperty(ISBN.getFieldName()));
    }

    @Test
    public void testInvalidKey() {
        FieldSet fs = new FieldSetImpl.Builder().add(PRICE).add(ISBN).build();
        PropertyContainer<String> properties = getPropertyContainer(fs);
        boolean exception = false;
        try {
            properties.setProperty("foo", "bar");
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test public void testGetKeys() {
        FieldSet fs = new FieldSetImpl.Builder().add(PRICE).add(ISBN).build();
        PropertyContainer<String> properties =  getPropertyContainer(fs);
        Set<String> fields = properties.getKeys();
        assertEquals(2, fields.size());
        assertTrue(fs.contains(PRICE.getFieldName()));
        assertTrue(fs.contains(ISBN.getFieldName()));
    }

    @Test
    public void testRepeatedProperty() {
        FieldSet fs =
            new FieldSetImpl.Builder().add(PRICE).add(CATEGORIES)
                .add(RELEASE_DATE).build();
        PropertyContainer<String> properties = getPropertyContainer(fs);

        properties.setProperty(PRICE.getFieldName(), 100);
        properties.setProperty(RELEASE_DATE.getFieldName(), 7777l);
        assertEquals(100, properties.getProperty(PRICE.getFieldName()));
        assertEquals(7777l, properties.getProperty(RELEASE_DATE.getFieldName()));

        List<String> categories =
            Arrays.<String> asList("cat0", "cat1", "cat2");
        assertFalse(properties.containsProperty(CATEGORIES.getFieldName()));
        assertNull(properties.getRepeatedProperties(CATEGORIES.getFieldName()));
        assertNull(properties.setRepeatedProperties(CATEGORIES.getFieldName(),
                                                    categories));

        List<String> storedCategories =
            properties.getRepeatedProperties(CATEGORIES.getFieldName());
        assertNotNull(storedCategories);
        assertEquals(categories, storedCategories);

        assertEquals(100, properties.getProperty(PRICE.getFieldName()));
        assertEquals(7777l, properties.getProperty(RELEASE_DATE.getFieldName()));

        properties.addRepeatedProperty(CATEGORIES.getFieldName(), "cat3");
        properties.addRepeatedProperty(CATEGORIES.getFieldName(), "cat4");
        storedCategories =
            properties.getRepeatedProperties(CATEGORIES.getFieldName());
        categories =
            Arrays.<String> asList("cat0", "cat1", "cat2", "cat3", "cat4");

        assertNotNull(storedCategories);
        assertEquals(categories, storedCategories);

        assertEquals(100, properties.getProperty(PRICE.getFieldName()));
        assertEquals(7777l, properties.getProperty(RELEASE_DATE.getFieldName()));

        assertEquals(categories, properties.setRepeatedProperties(CATEGORIES
            .getFieldName(), null));
        assertNull(properties.setRepeatedProperties(CATEGORIES.getFieldName(),
                                                    categories));
        assertEquals(categories, storedCategories);

        assertEquals(100, properties.getProperty(PRICE.getFieldName()));
        assertEquals(7777l, properties.getProperty(RELEASE_DATE.getFieldName()));

        assertEquals(categories, properties.setRepeatedProperties(CATEGORIES
            .getFieldName(), null));
        properties.addRepeatedProperty(CATEGORIES.getFieldName(), "cat4");
        assertEquals(Arrays.<String> asList("cat4"),
                     properties.getRepeatedProperties(CATEGORIES.getFieldName()));

        assertEquals(100, properties.getProperty(PRICE.getFieldName()));
        assertEquals(7777l, properties.getProperty(RELEASE_DATE.getFieldName()));
    }
}
