package recng.common;

import java.nio.ByteBuffer;

/**
 * A Float marshaller.
 *
 * @author jon
 *
 */
public class FloatMarshaller extends AbstractMarshaller<Float> {

    public FloatMarshaller(Float defaultValue) {
        super(defaultValue);
    }

    @Override
    public byte[] marshall(Object value) {
        return ByteBuffer.allocate(4).putFloat(getTypedValue(value)).array();
    }

    @Override
    public Float unmarshall(byte[] bytes) {
        checkByteArrayLength(bytes, 4);
        return ByteBuffer.wrap(bytes).getFloat();
    }

    @Override
    public Float parse(String s) {
        if (s.isEmpty())
            return getDefaultValue();
        return Float.valueOf(s);
    }
}
