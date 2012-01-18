package recng.recommendations.data;

import java.util.Map;

/**
 * Parses a row from a data file.
 *
 * @author jon
 */
public interface DataRowParser {
    Map<String, Object> parseLine(String line);
}
