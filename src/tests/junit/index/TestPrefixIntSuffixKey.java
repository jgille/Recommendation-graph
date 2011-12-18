package tests.junit.index;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import rectest.index.PrefixIntSuffixKey;
import rectest.index.Key;
import rectest.index.KeyFactory;

public class TestPrefixIntSuffixKey {

    private Key<String> getKey(String id) {
        return PrefixIntSuffixKey.Factory.getInstance().parse(id);
    }

    @Test public void testInt() {
        test("123");
    }

    @Test public void testPrefixInt() {
        test("P123");
    }

    @Test public void testIntSuffix() {
        test("123S");
    }

    @Test public void testPrefixIntSuffix() {
        test("P123S");
    }

    private void test(String id) {
        Key<String> key = getKey(id);
        assertEquals(id, key.getValue());
        Map<Key<String>, String> map = new HashMap<Key<String>, String>();
        map.put(key, id);
        assertEquals(map.get(key), id);
    }

    @Test
    public void testLeadingZeros() {
        KeyFactory<String> factory = PrefixIntSuffixKey.Factory.getInstance();
        String id1 = "a0123450cd";
        String id2 = "a00123450cd";
        assertTrue(factory.matches(id1));
        assertTrue(factory.matches(id2));
        assertFalse(factory.parse(id1).equals(factory.parse(id2)));
    }

    @Test public void testInvalidId() {
        KeyFactory<String> factory = PrefixIntSuffixKey.Factory.getInstance();
        String id = "abc";
        assertFalse(factory.matches(id));
        boolean exception = false;
        try {
            factory.parse(id);
        } catch (Key.KeyFormatException e) {
            exception = true;
        }
        assertTrue(exception);

        id = "abcde123ef";
        assertFalse(factory.matches(id));
        exception = false;
        try {
            factory.parse(id);
        } catch (Key.KeyFormatException e) {
            exception = true;
        }
        assertTrue(exception);

        id = "ab123efghi";
        assertFalse(factory.matches(id));
        exception = false;
        try {
            factory.parse(id);
        } catch (Key.KeyFormatException e) {
            exception = true;
        }
        assertTrue(exception);

        id = "ab123ef";
        assertTrue(factory.matches(id));
    }
}
