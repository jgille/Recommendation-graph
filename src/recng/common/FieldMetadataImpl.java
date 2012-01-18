package recng.common;


/**
 * Metadata for a field (key) in a key/value pair.
 *
 * @author Jon Ivmark
 */
public class FieldMetadataImpl implements FieldMetadata {
    private final String fieldName;
    private final Marshaller marshaller;
    private final Type type;
    private final boolean repeated;

    public FieldMetadataImpl(String fieldName, Type type) {
        this(fieldName, type, false);
    }

    public FieldMetadataImpl(String fieldName, Type type, boolean repeated) {
        this.fieldName = fieldName;
        this.marshaller = getMarshaller(type);
        this.type = type;
        this.repeated = repeated;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public Type getType() {
        return type;
    }

    public boolean isRepeated() {
        return repeated;
    }

    @Override public String toString() {
        return String.format("Name: %s, Type: %s, Repeated: %s)",
                             fieldName, type, repeated);
    }

    private static Marshaller getMarshaller(Type type) {
        switch (type) {
        case BYTE:
            return Marshallers.BYTE_MARSHALLER;
        case SHORT:
            return Marshallers.SHORT_MARSHALLER;
        case INTEGER:
            return Marshallers.INTEGER_MARSHALLER;
        case LONG:
            return Marshallers.LONG_MARSHALLER;
        case FLOAT:
            return Marshallers.FLOAT_MARSHALLER;
        case DOUBLE:
            return Marshallers.DOUBLE_MARSHALLER;
        case BOOLEAN:
            return Marshallers.BOOLEAN_MARSHALLER;
        case STRING:
            return Marshallers.STRING_MARSHALLER;
        case DATE:
            return Marshallers.DATE_MARSHALLER;
        default:
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}
