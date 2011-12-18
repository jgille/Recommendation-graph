package recng.recommendations;

import java.util.Map;

/**
 * Parses a row from a product (meta)data file.
 *
 * @author jon
 * 
 */
public interface PMDRowParser {
    Map<String, Object> parseRow(String row);
}
