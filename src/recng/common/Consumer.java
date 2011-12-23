package recng.common;

/**
 * Used to consume an object and produce an output. This could be pretty much
 * anything, but one use case is consuming rows of text read from a file an
 * producing some parsed output from it.
 * 
 * @author jon
 * 
 * @param <IN>
 *            The generic type of the cosumed object.
 * @param <OUT>
 *            The generic type of the produced object.
 */
public interface Consumer<IN, OUT> {

    OUT consume(IN in);
}
