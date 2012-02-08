package recng.common.io;

public class InvalidCSVRowException extends IllegalArgumentException {

    private static final long serialVersionUID = 20120113L;

    public InvalidCSVRowException(String message) {
        super(message);
    }
}
