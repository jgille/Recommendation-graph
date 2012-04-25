package recng.common;

/**
 * Classes used to represent an ID in some other (possibly more memory efficent)
 * way should implement this interface.
 * 
 * @author jon
 * 
 * @param <T>
 *            The generic type of the ID representation.
 */
public interface IDParser<T> {

    /**
     * Creates an ID from a string.
     */
    T parse(String id);

    /**
     * Gets the string value of an ID.
     */
    String serialize(T id);
}
