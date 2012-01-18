package tests.junit.db.nio;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

import recng.db.KVStore;
import recng.db.nio.ByteBufferKVStore;

/**
 * Tests {@link ByteBufferKVStore}.
 * 
 * @author jon
 * 
 */
public class TestByteBufferKVStore {

    @Test
    public void testPutGet() throws UnsupportedEncodingException {
        KVStore<String, byte[]> db = new ByteBufferKVStore();
        String key1 = "key1";
        byte[] value1 = "value1".getBytes("UTF8");
        db.put(key1, value1);
        String key2 = "key2";
        byte[] value2 = "value2".getBytes("UTF8");
        db.put(key2, value2);
        Assert.assertArrayEquals(value1, db.get(key1));
        Assert.assertArrayEquals(value2, db.get(key2));
        db.put(key2, value1);
        Assert.assertArrayEquals(value1, db.get(key2));
        Assert.assertArrayEquals(value1, db.remove(key2));
        Assert.assertNull(db.get(key2));
    }
}
