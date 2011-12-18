package tests.junit.index;

import java.util.Comparator;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.index.ArrayKVStore;

/**
 *
 * @author Jon Ivmark
 */
public class TestArrayKVStore  {

    private static class StringComparator implements Comparator<String> {
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }

    @Test public void testBuildAndGet() {
        ArrayKVStore.Builder<String, Integer> builder =
            new ArrayKVStore.Builder<String, Integer>(new StringComparator());
        String s1 = "4324326";
        String s2 = "984345983";
        String s3 = "21323230920";
        builder.put(s1, 1);
        builder.put(s2, 2);
        builder.put(s3, 3);
        ArrayKVStore<String, Integer> store = builder.get();
        int v1 = store.find(s1);
        int v2 = store.find(s2);
        int v3 = store.find(s3);
        assertEquals(1, v1);
        assertEquals(2, v2);
        assertEquals(3, v3);
    }

}
