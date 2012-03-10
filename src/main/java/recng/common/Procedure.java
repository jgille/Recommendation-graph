package recng.common;

/**
 * A procedure.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the procudure parameter.
 */
public interface Procedure<T> {

    /**
     * Applies the procedure.
     *
     * @return A flag to inform the object calling the procedure.
     */
    boolean apply(T element);
}
