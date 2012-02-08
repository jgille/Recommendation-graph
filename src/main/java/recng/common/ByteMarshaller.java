package recng.common;

/**
 * A Byte marshaller.
 *
 * @author jon
 *
 */
public class ByteMarshaller extends AbstractMarshaller<Byte> {

    public ByteMarshaller(Byte defaultValue) {
        super(defaultValue);
    }

    public byte[] marshall(Object value) {
        return new byte[] { getTypedValue(value) };
    }

    @Override
    public Byte unmarshall(byte[] bytes) {
        checkByteArrayLength(bytes, 1);
        return bytes[0];
    }

    @Override
    public Byte parse(String s) {
        if (s.isEmpty())
            return getDefaultValue();
        return Byte.valueOf(s);
    }
}
