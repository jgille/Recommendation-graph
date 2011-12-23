package recng.index;

/**
 * Wraps an ID of some kind.
 * 
 * @author jon
 * 
 * @param <K>
 */
public interface ID<K> {

    /**
     * Gets the wrapped ID.
     */
    K getID();
}
