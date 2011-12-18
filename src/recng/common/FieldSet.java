package recng.common;

import java.util.Set;

/**
 * Metadata for a set of fields, describing the valid
 * fields for some type of property container.
 *
 * @author Jon Ivmark
 */
public interface FieldSet {
    /**
     * Gets a metadata instance for a field.
     *
     * Will throw an exception if no metadata exists for this field.
     */
    <T> FieldMetadata<T> getFieldMetadata(String fieldName);

    <T> FieldMetadata<T> getFieldMetadataByOrdinal(int ordinal);

    /**
     * Checks if the field is valid.
     */
    boolean contains(String fieldName);

    /**
     * Gets the generic type of the values corresponding to this field.
     */
    FieldMetadata.Type typeOf(String fieldName);

    /**
     * Returns the fields contained in this set.
     */
    Set<String> getFields();

    /**
     * Returns the ordinal of this field. All implementing classes must guarantee
     * that this is:
     *
     * 1) Unique
     * 2) greater than or equal to zero and less than size().
     */
    int ordinal(String fieldName);

    /**
     * Returns the number of fields.
     */
    int size();
}
