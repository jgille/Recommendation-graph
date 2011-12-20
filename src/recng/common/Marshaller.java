package recng.common;

/**
 * Classes used to marshal and unmarshal objects should implement this interface.
 *
 * @author Jon Ivmark
 */
public interface Marshaller<V> {

    /**
     * Marshalls an object to a byte array.
     */
    byte[] marshall(V value);

    /**
     * Unmarshalls a byte array to an object.
     */
    V unmarshall(byte[] bytes);

    /**
     * Parses a string to an object.
     */
    V parse(String s);
}
