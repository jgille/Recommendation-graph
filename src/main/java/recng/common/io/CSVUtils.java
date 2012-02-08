package recng.common.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import recng.common.PropertyContainer;
import recng.common.PropertyContainerFactory;

/**
 * Helper class for reading CSV files.
 *
 * @author jon
 *
 */
public class CSVUtils {

    /**
     * Reads the entire file at once, returning all rows in a list.
     */
    public static List<String[]> readAll(String file, CSVDescriptor descriptor)
        throws IOException {
        List<String[]> res = new ArrayList<String[]>();
        CSVCursor<String[]> cursor = read(file, descriptor);
        try {
            String[] next;
            while ((next = cursor.nextRow()) != null) {
                res.add(next);
            }
        } finally {
            cursor.close();
        }
        return res;
    }

    /**
     * Gets a cursor used to read the rows one by one.
     *
     * NOTE: Make sure to close the cursor in a finally block.
     */
    public static CSVCursor<String[]> read(String file,
                                           CSVDescriptor descriptor)
        throws IOException {
        return new DefaultCSVCursor(file, descriptor);
    }

    /**
     * Reads the entire file at once, returning all rows in a list. Each row is
     * parsed into a {@link PropertyContainer} using the settings from the
     * provided {@link CSVDescriptor}.
     */
    public static List<PropertyContainer>
        readAndParseAll(String file, CSVDescriptor descriptor,
                        PropertyContainerFactory factory)
            throws IOException {
        List<PropertyContainer> res = new ArrayList<PropertyContainer>();
        RowCursor<PropertyContainer> cursor =
            readAndParse(file, descriptor, factory);
        try {
            PropertyContainer next;
            while ((next = cursor.nextRow()) != null) {
                res.add(next);
            }
        } finally {
            cursor.close();
        }
        return res;
    }

    /**
     * Gets a cursor used to read the rows one by one. Each row is parsed into a
     * {@link PropertyContainer} using the settings from the provided
     * {@link CSVDescriptor}.
     *
     * NOTE: Make sure to close the cursor in a finally block.
     */
    public static CSVPropertyCursor
        readAndParse(String file, CSVDescriptor descriptor,
                     PropertyContainerFactory factory) throws IOException {
        CSVCursor<String[]> cursor = read(file, descriptor);
        return new CSVPropertyCursor(cursor, descriptor, factory);
    }
}
