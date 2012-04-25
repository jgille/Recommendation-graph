package tests.junit.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.FieldType;
import recng.common.TableMetadata;
import recng.common.TableMetadataImpl;
import recng.common.PropertyContainer;

/**
 * Base class for testing {@link PropertyContainer}s.
 * 
 * @author jon
 * 
 */
public abstract class AbstractTestPropertyContainer {

    protected static final FieldMetadata PRICE =
        FieldMetadataImpl.create("Price",
                                 FieldType.INT);
    protected static final FieldMetadata ISBN =
        FieldMetadataImpl.create("ISBN",
                                 FieldType.STRING);

    protected static final FieldMetadata RELEASE_DATE =
        FieldMetadataImpl.create("ReleaseDate",
                                 FieldType.LONG);

    protected static final FieldMetadata CATEGORIES =
        new FieldMetadataImpl.Builder("Categories",
                                      FieldType.STRING)
            .setRepeated(true).build();

    protected static final FieldMetadata INT_LIST =
        new FieldMetadataImpl.Builder("IntList",
                                      FieldType.INT).setRepeated(true)
            .build();

    protected abstract PropertyContainer getPropertyContainer(TableMetadata fs);

    @Test
    public void testGetSetContainsProperty() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, ISBN,
                                                               RELEASE_DATE));
        PropertyContainer properties = getPropertyContainer(fs);

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
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, ISBN));
        PropertyContainer properties = getPropertyContainer(fs);
        boolean exception = false;
        try {
            properties.setProperty("foo", "bar");
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testGetKeys() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, ISBN));
        PropertyContainer properties = getPropertyContainer(fs);
        List<String> fields = properties.getKeys();
        assertEquals(2, fields.size());
        assertTrue(fs.contains(PRICE.getFieldName()));
        assertTrue(fs.contains(ISBN.getFieldName()));
    }

    @Test
    public void testAsMap() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, ISBN));
        PropertyContainer properties = getPropertyContainer(fs);
        Map<String, Object> expected = new HashMap<String, Object>();
        Map<String, Object> map = properties.asMap();
        assertEquals(expected, map);
    }

    @Test
    public void testRepeatedProperty() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, CATEGORIES,
                                                               RELEASE_DATE));
        PropertyContainer properties = getPropertyContainer(fs);

        properties.setProperty(PRICE.getFieldName(), 100);
        properties.setProperty(RELEASE_DATE.getFieldName(), 7777l);
        assertEquals(100, properties.getProperty(PRICE.getFieldName()));
        assertEquals(7777l, properties.getProperty(RELEASE_DATE.getFieldName()));

        List<Object> categories =
            Arrays.<Object> asList("cat0", "cat1", "cat2");
        assertFalse(properties.containsProperty(CATEGORIES.getFieldName()));
        assertNull(properties.getRepeatedProperties(CATEGORIES.getFieldName()));
        assertNull(properties.setRepeatedProperties(CATEGORIES.getFieldName(),
                                                    categories));

        List<Object> storedCategories =
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
            Arrays.<Object> asList("cat0", "cat1", "cat2", "cat3", "cat4");

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

    @Test
    public void testSetAndGet() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(PRICE, CATEGORIES,
                                                               RELEASE_DATE));
        PropertyContainer properties = getPropertyContainer(fs);
        properties.setProperty(PRICE.getFieldName(), 1);
        assertEquals(1, properties.getProperty(PRICE.getFieldName()));
    }

    @Test
    public void testRepeatedNegativeIntProperty() {
        TableMetadata fs = new TableMetadataImpl(Arrays.asList(INT_LIST));
        PropertyContainer properties = getPropertyContainer(fs);
        List<Object> ints =
            Arrays.<Object> asList(-1, -2, -Integer.MAX_VALUE);
        properties.setRepeatedProperties(INT_LIST.getFieldName(), ints);
        List<Object> res =
            properties.getRepeatedProperties(INT_LIST.getFieldName());
        assertEquals(ints, res);
    }
}
