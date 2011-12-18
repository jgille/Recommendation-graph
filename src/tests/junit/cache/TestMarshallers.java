package tests.junit.cache;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.common.Marshaller;
import recng.common.Marshallers;

/**
 * Tests for all marshallers in {@link recng.common.Marshallers}.
 *
 * @author Jon Ivmark
 */
public class TestMarshallers {

    @Test public void testByteMarshaller() {
        Marshaller<Byte> marshaller = Marshallers.BYTE_MARSHALLER;
        Byte b = Byte.valueOf("2");
        byte[] ba = marshaller.marshall(b);
        assertEquals(b, marshaller.unmarshall(ba));
    }

    @Test public void testShortMarshaller() {
        Marshaller<Short> marshaller = Marshallers.SHORT_MARSHALLER;
        Short s = (short)2;
        byte[] ba = marshaller.marshall(s);
        assertEquals(s, marshaller.unmarshall(ba));
    }

    @Test public void testIntMarshaller() {
        Marshaller<Integer> marshaller = Marshallers.INTEGER_MARSHALLER;
        Integer i = 2;
        byte[] ba = marshaller.marshall(i);
        assertEquals(i, marshaller.unmarshall(ba));
    }

    @Test public void testFloatMarshaller() {
        Marshaller<Float> marshaller = Marshallers.FLOAT_MARSHALLER;
        Float f = 2f;
        byte[] ba = marshaller.marshall(f);
        assertEquals(f, marshaller.unmarshall(ba), 0.000001f);
    }

    @Test public void testDoubleMarshaller() {
        Marshaller<Double> marshaller = Marshallers.DOUBLE_MARSHALLER;
        Double d = 2d;
        byte[] ba = marshaller.marshall(d);
        assertEquals(d, marshaller.unmarshall(ba), 0.000001d);
    }

    @Test public void testBooleanMarshaller() {
        Marshaller<Boolean> marshaller = Marshallers.BOOLEAN_MARSHALLER;
        Boolean b = true;
        byte[] ba = marshaller.marshall(b);
        assertEquals(true, marshaller.unmarshall(ba));
    }

    @Test public void testStringMarshaller() {
        Marshaller<String> marshaller = Marshallers.STRING_MARSHALLER;
        String s = "hello world";
        byte[] ba = marshaller.marshall(s);
        assertEquals(s, marshaller.unmarshall(ba));
    }
}
