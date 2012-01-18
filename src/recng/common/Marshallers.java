package recng.common;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Marshallers for all supported primitives. All marshallers are thread safe.
 *
 * @author Jon Ivmark
 */
public class Marshallers {

    public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd");

    private Marshallers() {
        // Prevent instantiation
    }

    public static final Marshaller BYTE_MARSHALLER =
        new Marshaller() {
            public byte[] marshall(Object value) {
                if (value == null)
                    return null;
                if (!(value instanceof Byte))
                    throw new IllegalArgumentException("Can not cast " + value
                        + " to a byte");
                return new byte[] { (Byte) value };
        }
        public Byte unmarshall(byte[] bytes) {
            return bytes[0];
        }
        public Byte parse(String s) {
            return Byte.valueOf(s);
        }
    };

    public static final Marshaller SHORT_MARSHALLER =
        new Marshaller() {
            public byte[] marshall(Object value) {
                if (value == null)
                    return null;
                if (!(value instanceof Short))
                    throw new IllegalArgumentException("Can not cast " + value
                        + " to a short");
                return ByteBuffer.allocate(2).putShort((Short) value).array();
        }
        public Short unmarshall(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getShort();
        }
        public Short parse(String s) {
            return Short.valueOf(s);
        }
    };

    public static final Marshaller INTEGER_MARSHALLER =
        new Marshaller() {
            public byte[] marshall(Object value) {
                if (value == null)
                    return null;
                if (!(value instanceof Integer))
                    throw new IllegalArgumentException("Can not cast " + value
                        + " to an integer");
                return ByteBuffer.allocate(4).putInt((Integer) value).array();
            }
        public Integer unmarshall(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getInt();
        }
        public Integer parse(String s) {
            return Integer.valueOf(s);
        }
    };

    public static final Marshaller LONG_MARSHALLER =
        new Marshaller() {
            public byte[] marshall(Object value) {
                if (value == null)
                    return null;
                if (!(value instanceof Long))
                    throw new IllegalArgumentException("Can not cast " + value
                        + " to a long");
                return ByteBuffer.allocate(8).putLong((Long) value).array();
        }
        public Long unmarshall(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getLong();
        }
        public Long parse(String s) {
            return Long.valueOf(s);
        }
    };

    public static final Marshaller DATE_MARSHALLER =
        new Marshaller() {
            public byte[] marshall(Object value) {
                if (value == null)
                    return null;
                if (!(value instanceof Date))
                    throw new IllegalArgumentException("Can not cast " + value
                        + " to a date");
                return ByteBuffer.allocate(8).putLong(((Date) value).getTime())
                    .array();
        }
        public Date unmarshall(byte[] bytes) {
            return new Date(ByteBuffer.wrap(bytes).getLong());
        }

            /* Note that this has to be synchronized due to the SDF. */
        public synchronized Date parse(String s) {
                if (s == null || s.isEmpty())
                    return null;
                try {
                    return DF.parse(s);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }
        }
    };

    public static final Marshaller FLOAT_MARSHALLER =
        new Marshaller() {
            public byte[] marshall(Object value) {
                if (value == null)
                    return null;
                if (!(value instanceof Float))
                    throw new IllegalArgumentException("Can not cast " + value
                        + " to a float");
                return ByteBuffer.allocate(4).putFloat((Float) value).array();
        }
        public Float unmarshall(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getFloat();
        }
        public Float parse(String s) {
                return Float.valueOf(s);
        }
    };

    public static final Marshaller DOUBLE_MARSHALLER =
        new Marshaller() {
            public byte[] marshall(Object value) {
                if (value == null)
                    return null;
                if (!(value instanceof Double))
                    throw new IllegalArgumentException("Can not cast " + value
                        + " to a double");
                return ByteBuffer.allocate(8).putDouble((Double) value).array();
        }
        public Double unmarshall(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getDouble();
        }
        public Double parse(String s) {
                return Double.valueOf(s);
        }
    };

    public static final Marshaller BOOLEAN_MARSHALLER =
        new Marshaller() {
            public byte[] marshall(Object value) {
                if (value == null)
                    return null;
                if (!(value instanceof Boolean))
                    throw new IllegalArgumentException("Can not cast " + value
                        + " to a boolean");
                return new byte[] { ((Boolean) value).booleanValue() ? (byte) 1
                    : (byte) 0 };
        }
        public Boolean unmarshall(byte[] bytes) {
            return bytes[0] == 1;
        }
        public Boolean parse(String s) {
            return Boolean.valueOf(s);
        }
    };

    public static final Marshaller STRING_MARSHALLER =
        new Marshaller() {
            public byte[] marshall(Object value) {
                if (value == null)
                    return null;
                if (!(value instanceof String))
                    throw new IllegalArgumentException("Can not cast " + value
                        + " to a String");
            try {
                    return ((String) value).getBytes("UTF8");
            } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
        public String unmarshall(byte[] bytes) {
            try {
                return new String(bytes, "UTF8");
            } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
        public String parse(String s) {
            return s;
        }
    };
}
