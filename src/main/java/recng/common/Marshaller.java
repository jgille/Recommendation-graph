package recng.common;

/**
 * Classes used to marshal and unmarshal objects should implement this
 * interface.
 * 
 * @author Jon Ivmark
 */
public interface Marshaller {

    /**
     * Marshalls an object to a byte array.
     */
    byte[] marshall(Object value);

    /**
     * Unmarshalls a byte array to an object.
     */
    Object unmarshall(byte[] bytes);

    /**
     * Parses a string to an object.
     */
    Object parse(String s);

    String serializeToString(Object value);
}
