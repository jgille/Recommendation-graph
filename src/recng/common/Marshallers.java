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

    public static final Marshaller<Byte> BYTE_MARSHALLER =
        new Marshaller<Byte>() {
        public byte[] marshall(Byte value) {
            return new byte[] {value};
        }
        public Byte unmarshall(byte[] bytes) {
            return bytes[0];
        }
        public Byte parse(String s) {
            return Byte.valueOf(s);
        }
    };

    public static final Marshaller<Short> SHORT_MARSHALLER =
        new Marshaller<Short>() {
        public byte[] marshall(Short value) {
            return ByteBuffer.allocate(2).putShort(value).array();
        }
        public Short unmarshall(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getShort();
        }
        public Short parse(String s) {
            return Short.valueOf(s);
        }
    };

    public static final Marshaller<Integer> INTEGER_MARSHALLER =
        new Marshaller<Integer>() {
        public byte[] marshall(Integer value) {
            return ByteBuffer.allocate(4).putInt(value).array();
        }
        public Integer unmarshall(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getInt();
        }
        public Integer parse(String s) {
            return Integer.valueOf(s);
        }
    };

    public static final Marshaller<Long> LONG_MARSHALLER =
        new Marshaller<Long>() {
        public byte[] marshall(Long value) {
            return ByteBuffer.allocate(8).putLong(value).array();
        }
        public Long unmarshall(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getLong();
        }
        public Long parse(String s) {
            return Long.valueOf(s);
        }
    };

    public static final Marshaller<Date> DATE_MARSHALLER =
        new Marshaller<Date>() {
        public byte[] marshall(Date value) {
            return ByteBuffer.allocate(8).putLong(value.getTime()).array();
        }
        public Date unmarshall(byte[] bytes) {
            return new Date(ByteBuffer.wrap(bytes).getLong());
        }
        public synchronized Date parse(String s) {
                try {
                    return DF.parse(s);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }
        }
    };

    public static final Marshaller<Float> FLOAT_MARSHALLER =
        new Marshaller<Float>() {
        public byte[] marshall(Float value) {
            return ByteBuffer.allocate(4).putFloat(value).array();
        }
        public Float unmarshall(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getFloat();
        }
        public Float parse(String s) {
            return Float.valueOf(s);
        }
    };

    public static final Marshaller<Double> DOUBLE_MARSHALLER =
        new Marshaller<Double>() {
        public byte[] marshall(Double value) {
            return ByteBuffer.allocate(8).putDouble(value).array();
        }
        public Double unmarshall(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getDouble();
        }
        public Double parse(String s) {
            return Double.valueOf(s);
        }
    };

    public static final Marshaller<Boolean> BOOLEAN_MARSHALLER =
        new Marshaller<Boolean>() {
        public byte[] marshall(Boolean value) {
            return new byte[] {value.booleanValue() ? (byte)1 : (byte)0};
        }
        public Boolean unmarshall(byte[] bytes) {
            return bytes[0] == 1;
        }
        public Boolean parse(String s) {
            return Boolean.valueOf(s);
        }
    };

    public static final Marshaller<String> STRING_MARSHALLER =
        new Marshaller<String>() {
        public byte[] marshall(String value) {
            try {
                return value.getBytes("UTF8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        }
        public String unmarshall(byte[] bytes) {
            try {
                return new String(bytes, "UTF8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        }
        public String parse(String s) {
            return s;
        }
    };
}
