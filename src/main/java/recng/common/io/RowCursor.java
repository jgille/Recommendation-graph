package recng.common.io;

import java.io.IOException;

/**
 * Classes used to iterate rows in a file might implement this interface,
 *
 * @author jon
 *
 * @param <E>
 */
public interface RowCursor<E> {

    /**
     * Gets the name of the file.
     */
    String getFileName();

    /**
     * Get the next row in the file.
     */
    E nextRow() throws IOException;

    /**
     * Gets the current row no.
     */
    int currentRow();

    /**
     * Close the cursor.
     *
     * NOTE: Make sure to always do this in a finally block.
     */
    void close() throws IOException;

}
