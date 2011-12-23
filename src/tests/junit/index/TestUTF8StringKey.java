package tests.junit.index;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.index.ID;
import recng.index.UTF8StringID;

public class TestUTF8StringKey {

    private ID<String> getKey(String id) {
        return UTF8StringID.Parser.getInstance().parse(id);
    }

    @Test public void testStringValue() {
        String id = "abc";
        ID<String> key = getKey(id);
        assertEquals(id, key.getID());
    }

    @Test public void testEquals() {
        String id1 = "a";
        ID<String> key1 = getKey(id1);
        String id2 = "b";
        ID<String> key2 = getKey(id2);

        assertEquals(key1, getKey(id1));
        assertEquals(getKey(id1), key1);
        assertFalse(key1.equals(key2));

        Map<ID<String>, String> map = new HashMap<ID<String>, String>();
        map.put(key1, id1);
        assertEquals(map.get(key1), id1);
    }
}
