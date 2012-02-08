package recng.common;

import java.nio.ByteBuffer;

/**
 * A Long marshaller.
 *
 * @author jon
 *
 */
public class LongMarshaller extends AbstractMarshaller<Long> {

    public LongMarshaller(Long defaultValue) {
        super(defaultValue);
    }

    @Override
    public byte[] marshall(Object value) {
        return ByteBuffer.allocate(8).putLong(getTypedValue(value)).array();
    }

    @Override
    public Long unmarshall(byte[] bytes) {
        checkByteArrayLength(bytes, 8);
        return ByteBuffer.wrap(bytes).getLong();
    }

    @Override
    public Long parse(String s) {
        if (s.isEmpty())
            return getDefaultValue();
        return Long.valueOf(s);
    }
}
