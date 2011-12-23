package recng.recommendations;

import java.util.Map;

import recng.common.Consumer;

/**
 * Reads and parses product data from file.
 *
 * @author jon
 *
 */
public interface ProductDataReader {

    /**
     * Reads product data.
     *
     * @param rowParser
     *            Used to parse rows in the file to a property map.
     */
    void readProductData(String file,
                         Consumer<String, Map<String, Object>> rowParser);
}
