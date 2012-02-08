package recng.common;

/**
 * A Boolean marshaller.
 *
 * @author jon
 *
 */
public class BooleanMarshaller extends AbstractMarshaller<Boolean> {

    public BooleanMarshaller(Boolean defaultValue) {
        super(defaultValue);
    }

    @Override
    public byte[] marshall(Object value) {
        return new byte[] { (getTypedValue(value)).booleanValue() ? (byte) 1
            : (byte) 0 };
    }

    @Override
    public Boolean unmarshall(byte[] bytes) {
        checkByteArrayLength(bytes, 1);
        return bytes[0] == 1;
    }

    @Override
    public Boolean parse(String s) {
        if (s.isEmpty())
            return getDefaultValue();
        return Boolean.valueOf(s);
    }
}
