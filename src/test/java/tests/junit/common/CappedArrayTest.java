package tests.junit.common;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import recng.common.CappedArray;

/**
 * Tests {@link CappedArray}.
 *
 * @author jon
 *
 */
public class CappedArrayTest {

    @Test
    public void testEmpty() {
        CappedArray<Integer> arr = new CappedArray<Integer>(10);
        Assert.assertEquals("Expected the array to be empty", 0, arr.size());
    }

    @Test
    public void testOneElement() {
        CappedArray<Integer> arr = new CappedArray<Integer>(10);
        Assert.assertEquals("Expected the array to be empty", 0, arr.size());
        arr.push(1);
        Assert.assertEquals("Wrong array size", 1, arr.size());
        Assert.assertEquals(Arrays.asList(1), arr.asList());
    }

    @Test
    public void testCap() {
        CappedArray<Integer> arr = new CappedArray<Integer>(3);
        Assert.assertEquals("Expected the array to be empty", 0, arr.size());
        arr.push(1);
        Assert.assertEquals("Wrong array size", 1, arr.size());
        arr.push(2);
        Assert.assertEquals("Wrong array size", 2, arr.size());
        arr.push(3);
        Assert.assertEquals("Wrong array size", 3, arr.size());
        Assert.assertEquals(Arrays.asList(1, 2, 3), arr.asList());
        arr.push(4);
        Assert.assertEquals("Wrong array size", 3, arr.size());
        Assert.assertEquals(Arrays.asList(2, 3, 4), arr.asList());
    }
}
