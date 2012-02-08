package recng.common;

import java.nio.ByteBuffer;

/**
 * A Double marshaller.
 *
 * @author jon
 *
 */
public class DoubleMarshaller extends AbstractMarshaller<Double> {

    public DoubleMarshaller(Double defaultValue) {
        super(defaultValue);
    }

    @Override
    public byte[] marshall(Object value) {
        return ByteBuffer.allocate(8).putDouble(getTypedValue(value)).array();
    }

    @Override
    public Double unmarshall(byte[] bytes) {
        checkByteArrayLength(bytes, 8);
        return ByteBuffer.wrap(bytes).getDouble();
    }

    @Override
    public Double parse(String s) {
        if (s.isEmpty())
            return getDefaultValue();
        return Double.valueOf(s);
    }
}
