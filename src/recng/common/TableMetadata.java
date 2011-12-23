package recng.common;

import java.util.Set;

/**
 * Metadata describing a property container or table with a fixed set of
 * propeties/fields.
 *
 * @author Jon Ivmark
 */
public interface TableMetadata {
    /**
     * Gets metadata for a field.
     *
     * Will throw an exception if no metadata exists for this field.
     */
    <T> FieldMetadata<T> getFieldMetadata(String fieldName);

    /**
     * Checks if the field is valid.
     */
    boolean contains(String fieldName);

    /**
     * Gets the type of the values corresponding to this field.
     */
    FieldMetadata.Type typeOf(String fieldName);

    /**
     * Returns the names of the fields.
     */
    Set<String> getFields();

    /**
     * Returns the ordinal of this field. All implementing classes must
     * guarantee that this is:
     * 
     * 1) Unique 2) greater than or equal to zero and less than size().
     *
     * A negative value means that the field does not exist.
     */
    int ordinal(String fieldName);

    /**
     * Returns the number of fields.
     */
    int size();
}
