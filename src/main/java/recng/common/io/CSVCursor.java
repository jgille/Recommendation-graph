package recng.common.io;

import java.util.List;

/**
 * Classes used to iterate rows in a csv file might should this interface,
 *
 * @author jon
 *
 */
public interface CSVCursor<E> extends RowCursor<E> {

    /**
     * Gets the column names, or null if unknown.
     */
    List<String> getColumnNames();
}
