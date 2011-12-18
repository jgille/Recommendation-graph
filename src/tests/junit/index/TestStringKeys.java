package tests.junit.index;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.index.IntKey;
import recng.index.Key;
import recng.index.LongKey;
import recng.index.PrefixIntSuffixKey;
import recng.index.PrefixLongSuffixKey;
import recng.index.StringKeys;
import recng.index.UTF8StringKey;

public class TestStringKeys {

    @Test public void testIntKey() {
        String id = "123";
        Key<String> key = StringKeys.parseKey(id);
        assertEquals(id, key.getValue());
        assertTrue(IntKey.class == key.getClass());
    }

    @Test public void testPrefixIntKey() {
        String id = "P123";
        Key<String> key = StringKeys.parseKey(id);
        assertEquals(id, key.getValue());
        assertTrue(PrefixIntSuffixKey.class == key.getClass());
    }

    @Test public void testIntSuffixKey() {
        String id = "123S";
        Key<String> key = StringKeys.parseKey(id);
        assertEquals(id, key.getValue());
        assertTrue(PrefixIntSuffixKey.class == key.getClass());
    }

    @Test public void testPrefixIntSuffixKey() {
        String id = "P123S";
        Key<String> key = StringKeys.parseKey(id);
        assertEquals(id, key.getValue());
        assertTrue(PrefixIntSuffixKey.class == key.getClass());
    }

    @Test public void testLongKey() {
        String id = "12345678912345";
        Key<String> key = StringKeys.parseKey(id);
        assertEquals(id, key.getValue());
        assertTrue(LongKey.class == key.getClass());
    }

    @Test public void testPrefixLongKey() {
        String id = "P12345678912345";
        Key<String> key = StringKeys.parseKey(id);
        assertEquals(id, key.getValue());
        assertTrue(PrefixLongSuffixKey.class == key.getClass());
    }

    @Test public void testLongSuffixKey() {
        String id = "12345678912345S";
        Key<String> key = StringKeys.parseKey(id);
        assertEquals(id, key.getValue());
        assertTrue(PrefixLongSuffixKey.class == key.getClass());
    }

    @Test public void testPrefixLongSuffixKey() {
        String id = "P12345678912345S";
        Key<String> key = StringKeys.parseKey(id);
        assertEquals(id, key.getValue());
        assertTrue(PrefixLongSuffixKey.class == key.getClass());
    }

    @Test public void testUTF8StringKey() {
        String id = "P1TP2F";
        Key<String> key = StringKeys.parseKey(id);
        assertEquals(id, key.getValue());
        assertTrue(UTF8StringKey.class == key.getClass());
    }
}
