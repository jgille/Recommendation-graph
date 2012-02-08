package tests.junit.common;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.junit.Test;

import recng.common.Marshaller;
import recng.common.DefaultMarshallers;

/**
 * Tests for all marshallers in {@link DefaultMarshallers}.
 *
 * @author jon
 *
 */
public class DefaultMarshallersTest {

    private void assertIllegalUnmarshall(Marshaller marshaller, byte[] bytes) {
        boolean exception = false;
        try {
            marshaller.unmarshall(bytes);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue("Expected an IAE when unmarshalling.", exception);
    }

    private void assertIllegalMarshall(Marshaller marshaller, Object value) {
        boolean exception = false;
        try {
            marshaller.marshall(value);
        } catch (ClassCastException e) {
            exception = true;
        }
        assertTrue("Expected an IAE when marshalling " + value, exception);
    }

    @Test
    public void testByteMarshaller() {
        Marshaller m = DefaultMarshallers.BYTE_MARSHALLER;

        // Marshall and unmarshall a value
        byte b = 1;
        byte[] bytes = m.marshall(b);
        assertArrayEquals(new byte[] { 1 }, bytes);
        assertEquals(b, m.unmarshall(bytes));

        // Test null value
        assertIllegalUnmarshall(m, null);
        // Test illegal array dimension
        assertIllegalUnmarshall(m, new byte[] { 1, 1 });
        // Test illegal object
        assertIllegalMarshall(m, "Foo");
    }

    @Test
    public void testShortMarshaller() {
        Marshaller m = DefaultMarshallers.SHORT_MARSHALLER;

        // Marshall and unmarshall a value
        short s = 2;
        byte[] bytes = m.marshall(s);
        assertArrayEquals(new byte[] { 0, 2 }, bytes);
        assertEquals(s, m.unmarshall(bytes));

        // Test null value
        assertIllegalUnmarshall(m, null);
        // Test illegal array dimension
        assertIllegalUnmarshall(m, new byte[] { 1, 1, 1 });
        // Test illegal object
        assertIllegalMarshall(m, "Foo");
    }

    @Test
    public void testIntegerMarshaller() {
        Marshaller m = DefaultMarshallers.INTEGER_MARSHALLER;

        // Marshall and unmarshall a value
        int i = 2;
        byte[] bytes = m.marshall(i);
        assertArrayEquals(new byte[] { 0, 0, 0, 2 }, bytes);
        assertEquals(i, m.unmarshall(bytes));

        // Test null value
        assertIllegalUnmarshall(m, null);
        // Test illegal array dimension
        assertIllegalUnmarshall(m, new byte[] { 1, 1, 1 });
        // Test illegal object
        assertIllegalMarshall(m, "Foo");
    }

    @Test
    public void testFloatMarshaller() {
        Marshaller m = DefaultMarshallers.FLOAT_MARSHALLER;

        // Marshall and unmarshall a value
        float f = 2f;
        byte[] bytes = m.marshall(f);
        assertEquals(f, (Float) m.unmarshall(bytes), 0.000001f);
        // Test null value
        assertIllegalUnmarshall(m, null);
        // Test illegal array dimension
        assertIllegalUnmarshall(m, new byte[] { 1, 1, 1 });
        // Test illegal object
        assertIllegalMarshall(m, "Foo");
    }

    @Test
    public void testDoubleMarshaller() {
        Marshaller m = DefaultMarshallers.DOUBLE_MARSHALLER;

        // Marshall and unmarshall a value
        double d = 2d;
        byte[] bytes = m.marshall(d);
        assertEquals(d, (Double) m.unmarshall(bytes), 0.000001d);

        // Test null value
        assertIllegalUnmarshall(m, null);
        // Test illegal array dimension
        assertIllegalUnmarshall(m, new byte[] { 1, 1, 1 });
        // Test illegal object
        assertIllegalMarshall(m, "Foo");
    }

    @Test
    public void testBooleanMarshaller() {
        Marshaller m = DefaultMarshallers.BOOLEAN_MARSHALLER;

        // Marshall and unmarshall a value
        boolean b = true;
        byte[] bytes = m.marshall(b);
        assertArrayEquals(new byte[] { 1 }, bytes);
        assertEquals(b, m.unmarshall(bytes));

        // Test null value
        assertIllegalUnmarshall(m, null);
        // Test illegal array dimension
        assertIllegalUnmarshall(m, new byte[] { 1, 1, 1 });
        // Test illegal object
        assertIllegalMarshall(m, "Foo");
    }

    @Test
    public void testStringMarshaller() throws UnsupportedEncodingException {
        Marshaller m = DefaultMarshallers.STRING_MARSHALLER;

        // Marshall and unmarshall a value
        String s = "123";
        byte[] bytes = m.marshall(s);
        assertArrayEquals(s.getBytes("UTF8"), bytes);
        assertEquals(s, m.unmarshall(bytes));

        // Test null value
        assertIllegalUnmarshall(m, null);
        // Test illegal object
        assertIllegalMarshall(m, 1);
    }

    @Test
    public void testDateMarshaller() {
        Marshaller m = DefaultMarshallers.DATE_MARSHALLER;

        // Marshall and unmarshall a value
        Date d = new Date();
        byte[] bytes = m.marshall(d);
        assertEquals(d, m.unmarshall(bytes));

        // Test null value
        assertIllegalUnmarshall(m, null);
        // Test illegal array dimension
        assertIllegalUnmarshall(m, new byte[] { 1, 1, 1 });
        // Test illegal object
        assertIllegalMarshall(m, "Foo");
    }
}
