package tests.junit.common;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.junit.Test;

import recng.common.Marshaller;
import recng.common.Marshallers;

/**
 * Tests for all marshallers in {@link Marshallers}.
 *
 * @author jon
 *
 */
public class MarshallersTest {

    @Test
    public void testByteMarshaller() {
        Marshaller m = Marshallers.BYTE_MARSHALLER;

        // Marshall and unmarshall a value
        byte b = 1;
        byte[] bytes = m.marshall(b);
        assertArrayEquals(new byte[] { 1 }, bytes);
        assertEquals(b, m.unmarshall(bytes));

        // Test null value
        bytes = m.marshall(null);
        assertArrayEquals(new byte[0], bytes);
        assertNull(m.unmarshall(bytes));

        // Test illegal array dimension
        bytes = new byte[] { 1, 1 };
        boolean exception = false;
        try {
            m.unmarshall(bytes);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);

        // Test illegal object
        exception = false;
        try {
            m.marshall("Foo");
        } catch (ClassCastException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testShortMarshaller() {
        Marshaller m = Marshallers.SHORT_MARSHALLER;

        // Marshall and unmarshall a value
        short s = 2;
        byte[] bytes = m.marshall(s);
        assertArrayEquals(new byte[] { 0, 2 }, bytes);
        assertEquals(s, m.unmarshall(bytes));

        // Test null value
        bytes = m.marshall(null);
        assertArrayEquals(new byte[0], bytes);
        assertNull(m.unmarshall(bytes));

        // Test illegal array dimension
        bytes = new byte[] { 1, 1, 1 };
        boolean exception = false;
        try {
            m.unmarshall(bytes);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);

        // Test illegal object
        exception = false;
        try {
            m.marshall("Foo");
        } catch (ClassCastException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testIntegerMarshaller() {
        Marshaller m = Marshallers.INTEGER_MARSHALLER;

        // Marshall and unmarshall a value
        int i = 2;
        byte[] bytes = m.marshall(i);
        assertArrayEquals(new byte[] { 0, 0, 0, 2 }, bytes);
        assertEquals(i, m.unmarshall(bytes));

        // Test null value
        bytes = m.marshall(null);
        assertArrayEquals(new byte[0], bytes);
        assertNull(m.unmarshall(bytes));

        // Test illegal array dimension
        bytes = new byte[] { 1, 1, 1, 1, 1 };
        boolean exception = false;
        try {
            m.unmarshall(bytes);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);

        // Test illegal object
        exception = false;
        try {
            m.marshall("Foo");
        } catch (ClassCastException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testFloatMarshaller() {
        Marshaller m = Marshallers.FLOAT_MARSHALLER;

        // Marshall and unmarshall a value
        float f = 2f;
        byte[] bytes = m.marshall(f);
        assertEquals(f, (Float) m.unmarshall(bytes), 0.000001f);

        // Test null value
        bytes = m.marshall(null);
        assertArrayEquals(new byte[0], bytes);
        assertNull(m.unmarshall(bytes));

        // Test illegal array dimension
        bytes = new byte[] { 1, 1, 1, 1, 1 };
        boolean exception = false;
        try {
            m.unmarshall(bytes);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);

        // Test illegal object
        exception = false;
        try {
            m.marshall("Foo");
        } catch (ClassCastException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testDoubleMarshaller() {
        Marshaller m = Marshallers.DOUBLE_MARSHALLER;

        // Marshall and unmarshall a value
        double d = 2d;
        byte[] bytes = m.marshall(d);
        assertEquals(d, (Double) m.unmarshall(bytes), 0.000001d);

        // Test null value
        bytes = m.marshall(null);
        assertArrayEquals(new byte[0], bytes);
        assertNull(m.unmarshall(bytes));

        // Test illegal array dimension
        bytes = new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        boolean exception = false;
        try {
            m.unmarshall(bytes);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);

        // Test illegal object
        exception = false;
        try {
            m.marshall("Foo");
        } catch (ClassCastException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testBooleanMarshaller() {
        Marshaller m = Marshallers.BOOLEAN_MARSHALLER;

        // Marshall and unmarshall a value
        boolean b = true;
        byte[] bytes = m.marshall(b);
        assertArrayEquals(new byte[] { 1 }, bytes);
        assertEquals(b, m.unmarshall(bytes));

        // Test null value
        bytes = m.marshall(null);
        assertArrayEquals(new byte[0], bytes);
        assertEquals(Boolean.FALSE, m.unmarshall(bytes));

        // Test illegal array dimension
        bytes = new byte[] { 1, 1 };
        boolean exception = false;
        try {
            m.unmarshall(bytes);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);

        // Test illegal object
        exception = false;
        try {
            m.marshall("Foo");
        } catch (ClassCastException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testStringMarshaller() throws UnsupportedEncodingException {
        Marshaller m = Marshallers.STRING_MARSHALLER;

        // Marshall and unmarshall a value
        String s = "123";
        byte[] bytes = m.marshall(s);
        assertArrayEquals(s.getBytes("UTF8"), bytes);
        assertEquals(s, m.unmarshall(bytes));

        // Test null value
        bytes = m.marshall(null);
        assertArrayEquals(new byte[0], bytes);
        assertEquals("", m.unmarshall(bytes));

        // Test illegal object
        boolean exception = false;
        try {
            m.marshall(1);
        } catch (ClassCastException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testDateMarshaller() {
        Marshaller m = Marshallers.DATE_MARSHALLER;

        // Marshall and unmarshall a value
        Date d = new Date();
        byte[] bytes = m.marshall(d);
        assertEquals(d, m.unmarshall(bytes));

        // Test null value
        bytes = m.marshall(null);
        assertArrayEquals(new byte[0], bytes);
        assertNull(m.unmarshall(bytes));

        // Test illegal array dimension
        bytes = new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        boolean exception = false;
        try {
            m.unmarshall(bytes);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);

        // Test illegal object
        exception = false;
        try {
            m.marshall("Foo");
        } catch (ClassCastException e) {
            exception = true;
        }
        assertTrue(exception);
    }

}
