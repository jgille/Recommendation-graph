package tests.junit.index;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.index.ID;
import recng.index.IDPattern;
import recng.index.IDFormatException;
import recng.index.PrefixIntSuffixID;

/**
 * Tests IDs matching the {@link PrefixIntSuffixID} pattern.
 * 
 * @author jon
 * 
 */
public class TestPrefixIntSuffixKey {

    private ID<String> getKey(String id) {
        return PrefixIntSuffixID.Parser.getInstance().parse(id);
    }

    @Test
    public void testInt() {
        test("123");
    }

    @Test
    public void testPrefixInt() {
        test("P123");
    }

    @Test
    public void testIntSuffix() {
        test("123S");
    }

    @Test
    public void testPrefixIntSuffix() {
        test("P123S");
    }

    private void test(String id) {
        ID<String> key = getKey(id);
        assertEquals(id, key.getID());
        Map<ID<String>, String> map = new HashMap<ID<String>, String>();
        map.put(key, id);
        assertEquals(map.get(key), id);
    }

    @Test
    public void testLeadingZeros() {
        IDPattern<String> factory = PrefixIntSuffixID.Parser.getInstance();
        String id1 = "a0123450cd";
        String id2 = "a00123450cd";
        assertTrue(factory.matches(id1));
        assertTrue(factory.matches(id2));
        assertFalse(factory.parse(id1).equals(factory.parse(id2)));
    }

    @Test
    public void testInvalidId() {
        IDPattern<String> factory = PrefixIntSuffixID.Parser.getInstance();
        String id = "abc";
        assertFalse(factory.matches(id));
        boolean exception = false;
        try {
            factory.parse(id);
        } catch (IDFormatException e) {
            exception = true;
        }
        assertTrue(exception);

        id = "abcde123ef";
        assertFalse(factory.matches(id));
        exception = false;
        try {
            factory.parse(id);
        } catch (IDFormatException e) {
            exception = true;
        }
        assertTrue(exception);

        id = "ab123efghi";
        assertFalse(factory.matches(id));
        exception = false;
        try {
            factory.parse(id);
        } catch (IDFormatException e) {
            exception = true;
        }
        assertTrue(exception);

        id = "ab123ef";
        assertTrue(factory.matches(id));
    }
}
