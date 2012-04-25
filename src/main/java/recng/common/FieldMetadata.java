package recng.common;


/**
 * Metadata for a field (key) in a key/value pair.
 *
 * @author jon
 *
 */
public interface FieldMetadata {

    public static final FieldMetadata ID =
        new FieldMetadataImpl.Builder("__id", FieldType.STRING).setRequired(true)
            .build();

    /**
     * Gets the field name.
     */
    String getFieldName();

    /**
     * Returns a marshaller that can be used to marshal the value to and from a
     * binary representation.
     */
    Marshaller getMarshaller();

    /**
     * Returns the type of the mapped value for this field.
     */
    FieldType getType();

    /**
     * Returns whether or not this field is repeated (a list) or not.
     */
    boolean isRepeated();

    /**
     * Gets the default value for this field, or null if no default value.
     */
    Object getDefaultValue();

    /**
     * Indicates if this field is required or not (i.e. if it allows null
     * values).
     */
    boolean isRequired();
}
