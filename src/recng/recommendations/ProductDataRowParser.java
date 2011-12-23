package recng.recommendations;

import java.util.Map;

/**
 * Parses a row from a product data file.
 * 
 * @author jon
 *
 */
public interface ProductDataRowParser {
    Map<String, Object> parseRow(String row);
}
