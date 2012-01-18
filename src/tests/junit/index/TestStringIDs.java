package tests.junit.index;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.index.IntID;
import recng.index.ID;
import recng.index.LongID;
import recng.index.PrefixIntSuffixID;
import recng.index.PrefixLongSuffixID;
import recng.index.StringIDs;
import recng.index.UTF8StringID;

/**
 * Tests {@link StringIDs}.
 * 
 * @author jon
 * 
 */
public class TestStringIDs {

    @Test
    public void testIntKey() {
        String id = "123";
        ID<String> key = StringIDs.parseKey(id);
        assertEquals(id, key.getID());
        assertTrue(IntID.class == key.getClass());
    }

    @Test
    public void testPrefixIntKey() {
        String id = "P123";
        ID<String> key = StringIDs.parseKey(id);
        assertEquals(id, key.getID());
        assertTrue(PrefixIntSuffixID.class == key.getClass());
    }

    @Test
    public void testIntSuffixKey() {
        String id = "123S";
        ID<String> key = StringIDs.parseKey(id);
        assertEquals(id, key.getID());
        assertTrue(PrefixIntSuffixID.class == key.getClass());
    }

    @Test
    public void testPrefixIntSuffixKey() {
        String id = "P123S";
        ID<String> key = StringIDs.parseKey(id);
        assertEquals(id, key.getID());
        assertTrue(PrefixIntSuffixID.class == key.getClass());
    }

    @Test
    public void testLongKey() {
        String id = "12345678912345";
        ID<String> key = StringIDs.parseKey(id);
        assertEquals(id, key.getID());
        assertTrue(LongID.class == key.getClass());
    }

    @Test
    public void testPrefixLongKey() {
        String id = "P12345678912345";
        ID<String> key = StringIDs.parseKey(id);
        assertEquals(id, key.getID());
        assertTrue(PrefixLongSuffixID.class == key.getClass());
    }

    @Test
    public void testLongSuffixKey() {
        String id = "12345678912345S";
        ID<String> key = StringIDs.parseKey(id);
        assertEquals(id, key.getID());
        assertTrue(PrefixLongSuffixID.class == key.getClass());
    }

    @Test
    public void testPrefixLongSuffixKey() {
        String id = "P12345678912345S";
        ID<String> key = StringIDs.parseKey(id);
        assertEquals(id, key.getID());
        assertTrue(PrefixLongSuffixID.class == key.getClass());
    }

    @Test
    public void testUTF8StringKey() {
        String id = "P1TP2F";
        ID<String> key = StringIDs.parseKey(id);
        assertEquals(id, key.getID());
        assertTrue(UTF8StringID.class == key.getClass());
    }
}
