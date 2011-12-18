package rectest.common;

/**
 * Classes used to marshal and unmarshal objects should implement this interface.
 *
 * @author Jon Ivmark
 */
public interface Marshaller<V> {
    byte[] marshall(V value);
    V unmarshall(byte[] bytes);
    V parse(String s);
}
