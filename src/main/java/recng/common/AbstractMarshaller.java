package recng.common;

/**
 * Base marshaller class.
 *
 * @author jon
 *
 */
abstract class AbstractMarshaller<E> implements Marshaller {

    private final E defaultValue;

    protected AbstractMarshaller(E defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String serializeToString(Object value) {
        return String.valueOf(getTypedValue(value));
    }

    protected void checkByteArrayLength(byte[] bytes, int expectedLength) {
        if (bytes == null)
            throw new IllegalArgumentException("Null byte array not allowed");
        if (bytes.length != expectedLength) {
            String message =
                String.format("Invalid byte array length, " +
                    "expected %s but got %s bytes",
                              expectedLength,
                              bytes.length);
            throw new IllegalArgumentException(message);
        }
    }

    protected E getDefaultValue() {
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    protected E getTypedValue(Object value) {
        if (value != null)
            return (E) value;
        throw new IllegalArgumentException("Null value not allowed!");
    }
}