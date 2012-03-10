package recng.common;

import java.nio.ByteBuffer;

/**
 * A Short marshaller.
 * 
 * @author jon
 * 
 */
public class ShortMarshaller extends AbstractMarshaller<Short> {

    public ShortMarshaller(Short defaultValue) {
        super(defaultValue);
    }

    @Override
    public byte[] marshall(Object value) {
        return ByteBuffer.allocate(2).putShort(getTypedValue(value)).array();
    }

    @Override
    public Short unmarshall(byte[] bytes) {
        checkByteArrayLength(bytes, 2);
        return ByteBuffer.wrap(bytes).getShort();
    }

    @Override
    public Short parse(String s) {
        if (s.isEmpty())
            return getDefaultValue();
        return Short.valueOf(s);
    }
}
