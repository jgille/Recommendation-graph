package rectest.common;


/**
 * Metadata for a field (key) in a key/value pair.
 *
 * @author Jon Ivmark
 */
public interface FieldMetadata<T> {

    public static final String IS_VALID = "__IS_VALID";
    public static final String CATEGORIES = "__CATEGORIES";

    /**
     * Gets the field name.
     */
    String getFieldName();

    /**
     * Returns a marshaller that can be used to marshal the value
     * to and from a byte array representation.
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
