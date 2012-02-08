package recng.index;

/**
 * Thrown when trying to parse a value that doesn't match the pattern for the ID
 * in question.
 * 
 * @see IDPattern
 * 
 * @author jon
 * 
 */
public class IDFormatException extends IllegalArgumentException {
    private static final long serialVersionUID = 201112210841L;

    public IDFormatException(String msg) {
        super(msg);
    }
}