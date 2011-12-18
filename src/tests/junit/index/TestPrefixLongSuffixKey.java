package tests.junit.index;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.index.Key;
import recng.index.KeyFactory;
import recng.index.PrefixLongSuffixKey;

public class TestPrefixLongSuffixKey {

    private Key<String> getKey(String id) {
        return PrefixLongSuffixKey.Factory.getInstance().parse(id);
    }

    @Test public void testLong() {
        test("123456789123456");
    }

    @Test public void testPrefixLong() {
        test("P123456789123456");
    }

    @Test public void testLongSuffix() {
        test("123456789123456S");
    }

    @Test public void testPrefixLongSuffix() {
        test("P123456789123456S");
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
        KeyFactory<String> factory = PrefixLongSuffixKey.Factory.getInstance();
        String id1 = "a0123450437894cd";
        String id2 = "a00123450437894cd";
        assertTrue(factory.matches(id1));
        assertTrue(factory.matches(id2));
        assertFalse(factory.parse(id1).equals(factory.parse(id2)));
    }

    @Test public void testInvalidId() {
        KeyFactory<String> factory = PrefixLongSuffixKey.Factory.getInstance();
        String id = "abc";
        assertFalse(factory.matches(id));
        boolean exception = false;
        try {
            factory.parse(id);
        } catch (Key.KeyFormatException e) {
            exception = true;
        }
        assertTrue(exception);

        id = "abcde12345678910ef";
        assertFalse(factory.matches(id));
        exception = false;
        try {
            factory.parse(id);
        } catch (Key.KeyFormatException e) {
            exception = true;
        }
        assertTrue(exception);

        id = "ab123345678910efghi";
        assertFalse(factory.matches(id));
        exception = false;
        try {
            factory.parse(id);
        } catch (Key.KeyFormatException e) {
            exception = true;
        }
        assertTrue(exception);

        id = "ab123345678910ef";
        assertTrue(factory.matches(id));
    }
}
