package rectest.common;


/**
 * Metadata for a field (key) in a key/value pair.
 *
 * @author Jon Ivmark
 */
public class FieldMetadataImpl<T> implements FieldMetadata<T> {
    private final String fieldName;
    private final Marshaller<T> marshaller;
    private final Type type;
    private final boolean repeated;

    public FieldMetadataImpl(String fieldName, Marshaller<T> marshaller,
                             Type type) {
        this(fieldName, marshaller, type, false);
    }

    public FieldMetadataImpl(String fieldName, Marshaller<T> marshaller,
                             Type type, boolean repeated) {
        this.fieldName = fieldName;
        this.marshaller = marshaller;
        this.type = type;
        this.repeated = repeated;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Marshaller<T> getMarshaller() {
        return marshaller;
    }

    public Type getType() {
        return type;
    }

    public boolean isRepeated() {
        return repeated;
    }

    @Override public String toString() {
        return String.format("%s (%s)", fieldName, type);
    }
}
