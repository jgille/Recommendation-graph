package tests.junit.index;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.index.ID;
import recng.index.IDPattern;
import recng.index.IDFormatException;
import recng.index.LongID;

/**
 * Tests IDs matching the {@link LongID} pattern.
 * 
 * @author jon
 * 
 */
public class TestLongKey {

    private ID<String> getKey(String id) {
        return LongID.Parser.getInstance().parse(id);
    }

    @Test
    public void testGetValue() {
        String id = "1234567891234";
        ID<String> key = getKey(id);
        assertEquals(id, key.getID());
    }

    @Test
    public void testEquals() {
        String id1 = "1234567891234";
        ID<String> key1 = getKey(id1);
        String id2 = "2345783929298";
        ID<String> key2 = getKey(id2);

        assertEquals(key1, getKey(id1));
        assertEquals(getKey(id1), key1);
        assertFalse(key1.equals(key2));

        Map<ID<String>, String> map = new HashMap<ID<String>, String>();
        map.put(key1, id1);
        assertEquals(map.get(key1), id1);
    }

    @Test
    public void testInvalidId() {
        IDPattern<String> factory = LongID.Parser.getInstance();
        String id = "abc2";
        assertFalse(factory.matches(id));
        boolean exception = false;
        try {
            factory.parse(id);
        } catch (IDFormatException e) {
            exception = true;
        }
        assertTrue(exception);

        id = "00123554354545443";
        assertFalse(factory.matches(id));
    }
}
