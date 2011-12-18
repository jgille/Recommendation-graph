package tests.junit.index;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.index.Key;
import recng.index.UTF8StringKey;

public class TestUTF8StringKey {

    private Key<String> getKey(String id) {
        return UTF8StringKey.Factory.getInstance().parse(id);
    }

    @Test public void testStringValue() {
        String id = "abc";
        Key<String> key = getKey(id);
        assertEquals(id, key.getValue());
    }

    @Test public void testEquals() {
        String id1 = "a";
        Key<String> key1 = getKey(id1);
        String id2 = "b";
        Key<String> key2 = getKey(id2);

        assertEquals(key1, getKey(id1));
        assertEquals(getKey(id1), key1);
        assertFalse(key1.equals(key2));

        Map<Key<String>, String> map = new HashMap<Key<String>, String>();
        map.put(key1, id1);
        assertEquals(map.get(key1), id1);
    }
}
