package recng.index;

/**
 * Used to created ID wrappers that might store the String ID more efficently.
 *
 * @author jon
 *
 * @param <K>
 */
public interface IDPattern<K> {

    /**
     * Checks if the ID matches this pattern.
     */
    boolean matches(String id);

    /**
     * Gets a wrapper object for the ID.
     */
    ID<K> parse(String id) throws IDFormatException;

}
