package recng.recommendations.data;

import java.io.IOException;

/**
 * Reads and parses data (product data for instance) from file.
 *
 * @author jon
 *
 */
public interface DataReader {

    /**
     * Reads and parses data from file.
     */
    void readFile(String file) throws IOException;
}
