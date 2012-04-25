package recng.common;

/**
 * Metadata for a field (key) in a key/value pair.
 *
 * @author Jon Ivmark
 */
public class FieldMetadataImpl implements FieldMetadata {
    private final String fieldName;
    private final Marshaller marshaller;
    private final FieldType type;
    private final boolean repeated;
    private final boolean required;
    private final Object defaultValue;

    private FieldMetadataImpl(String fieldName, FieldType type, boolean repeated,
                              boolean required, Object defaultValue) {
        this.fieldName = fieldName;
        this.marshaller = type.createMarshaller(defaultValue);
        this.type = type;
        this.repeated = repeated;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    /**
     * Creates a non required, non repeated, no default value
     * {@link FieldMetadata} instance.
     */
    public static FieldMetadata create(String fieldName, FieldType type) {
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
    public FieldType getType() {
        return type;
    }

    @Override
    public boolean isRepeated() {
        return repeated;
    }

    @Override
    public String toString() {
        return String.format("%s (type: %s, repeated: %s)",
                             fieldName, type, repeated);
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
        private final FieldType type;

        private boolean repeated = false;
        private boolean required = false;
        private Object defaultValue;

        public Builder(String fieldName, FieldType type) {
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
