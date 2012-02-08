package recng.common;

import java.nio.ByteBuffer;

/**
 * An Integer marshaller.
 *
 * @author jon
 *
 */
public class IntegerMarshaller extends AbstractMarshaller<Integer> {

    public IntegerMarshaller(Integer defaultValue) {
        super(defaultValue);
    }

    @Override
    public byte[] marshall(Object value) {
        return ByteBuffer.allocate(4).putInt(getTypedValue(value)).array();
    }

    @Override
    public Integer unmarshall(byte[] bytes) {
        checkByteArrayLength(bytes, 4);
        return ByteBuffer.wrap(bytes).getInt();
    }

    @Override
    public Integer parse(String s) {
        if (s.isEmpty())
            return getDefaultValue();
        return Integer.valueOf(s);
    }
}
