package tests.junit.index;

import static org.junit.Assert.*;
import org.junit.Test;

import rectest.index.SplitKeyKVStore;;

/**
 *
 * @author Jon Ivmark
 */
public class TestSplitKeyKVStore  {

    @Test public void testBuildAndGet() {
        SplitKeyKVStore.Builder<Integer> builder = new SplitKeyKVStore.Builder<Integer>(10);
        runTest(builder);
    }

    @Test public void testLongSplitSize() {
        SplitKeyKVStore.Builder<Integer> builder = new SplitKeyKVStore.Builder<Integer>(300);
        runTest(builder);
    }

    @Test public void testZeroSplitSize() {
        SplitKeyKVStore.Builder<Integer> builder = new SplitKeyKVStore.Builder<Integer>(0);
        runTest(builder);
    }

    @Test public void testNegativeSplitSize() {
        SplitKeyKVStore.Builder<Integer> builder = new SplitKeyKVStore.Builder<Integer>(-1);
        runTest(builder);
    }

    private void runTest(SplitKeyKVStore.Builder<Integer> builder) {
        String k1 = "432326";
        String k2 = "98434583";
        String k3 = "2132320920";
        builder.put(k1, 1);
        builder.put(k2, 2);
        builder.put(k3, 3);
        SplitKeyKVStore<Integer> store = builder.get();
        int v1 = store.get(k1);
        int v2 = store.get(k2);
        int v3 = store.get(k3);
        assertEquals(1, v1);
        assertEquals(2, v2);
        assertEquals(3, v3);
    }
}
