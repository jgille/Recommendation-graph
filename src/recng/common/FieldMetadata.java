package recng.common;

/**
 * Metadata for a field (key) in a key/value pair.
 *
 * @author jon
 * 
 * @param <T>
 *            The generic type of the mapped values for this field.
 */
public interface FieldMetadata<T> {

    /**
     * Gets the field name.
     */
    String getFieldName();

    /**
     * Returns a marshaller that can be used to marshal the value to and from a
     * binary representation.
     */
    Marshaller<T> getMarshaller();

    /**
     * Returns the type of the mapped value for this field.
     */
    Type getType();

    /**
     * Returns whether or not this field is repeated (a list) or not.
     */
    boolean isRepeated();

    public static enum Type {
        BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BOOLEAN, STRING, DATE;
    }
}
