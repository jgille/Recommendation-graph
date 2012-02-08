package recng.common;

import java.util.Date;

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
    private final boolean required;
    private final Object defaultValue;

    private FieldMetadataImpl(String fieldName, Type type, boolean repeated,
                              boolean required, Object defaultValue) {
        this.fieldName = fieldName;
        this.marshaller = createMarshaller(type, defaultValue);
        this.type = type;
        this.repeated = repeated;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    /**
     * Creates a non required, non repeated, no default value
     * {@link FieldMetadata} instance.
     */
    public static FieldMetadata create(String fieldName, Type type) {
        return new Builder(fieldName, type).build();
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public Marshaller getMarshaller() {
        return marshaller;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isRepeated() {
        return repeated;
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Type: %s, Repeated: %s)",
                             fieldName, type, repeated);
    }

    private static Marshaller createMarshaller(Type type, Object defaultValue) {
        switch (type) {
        case BYTE:
            return new ByteMarshaller((Byte) defaultValue);
        case SHORT:
            return new ShortMarshaller((Short) defaultValue);
        case INT:
            return new IntegerMarshaller((Integer) defaultValue);
        case LONG:
            return new LongMarshaller((Long) defaultValue);
        case FLOAT:
            return new FloatMarshaller((Float) defaultValue);
        case DOUBLE:
            return new DoubleMarshaller((Double) defaultValue);
        case BOOLEAN:
            return new BooleanMarshaller((Boolean) defaultValue);
        case STRING:
            return new StringMarshaller((String) defaultValue);
        case DATE:
            return new DateMarshaller((Date) defaultValue);
        default:
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    public static class Builder {
        private final String fieldName;
        private final Type type;

        private boolean repeated = false;
        private boolean required = false;
        private Object defaultValue;

        public Builder(String fieldName, Type type) {
            this.fieldName = fieldName;
            this.type = type;
        }

        public Builder setRepeated(boolean repeated) {
            this.repeated = repeated;
            return this;
        }

        public Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public FieldMetadata build() {
            return new FieldMetadataImpl(fieldName, type, repeated, required,
                                         defaultValue);
        }
    }
}
