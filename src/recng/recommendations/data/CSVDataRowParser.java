package recng.recommendations.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import recng.common.Marshaller;
import recng.common.TableMetadata;
import recng.common.FieldMetadata;

/**
 * Parses data from a row in a csv file into a key/value map.
 *
 * @author jon
 *
 */
public class CSVDataRowParser implements DataRowParser {

    private static final String ESCAPED_DOUBLE_QUOTE = "__esc_double_quote";

    private static final Pattern COLUMN_PATTERN =
        Pattern.compile("(^|;)\"([^\"]*)\"");

    private final TableMetadata metadata;

    /**
     * Constructs a new parser.
     *
     * @param metadata
     *            Describes the columns in the csv file.
     */
    public CSVDataRowParser(TableMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Map<String, Object> parseLine(String line) {
        if (line == null || line.isEmpty())
            return Collections.emptyMap();
        line = line.replace("\\\"", ESCAPED_DOUBLE_QUOTE);
        Map<String, Object> res = new HashMap<String, Object>();
        int i = 0;
        Matcher m = COLUMN_PATTERN.matcher(line);
        while (m.find()) {
            String value = m.group(2);
            value = value.replace(ESCAPED_DOUBLE_QUOTE, "\"");
            value = value.replace("\\", ""); // Remove all escaping backslashes
            FieldMetadata fieldMetadata = metadata.getFieldMetadata(i++);
            Marshaller marshaller = fieldMetadata.getMarshaller();
            res.put(fieldMetadata.getFieldName(), marshaller.parse(value));
        }
        return res;
    }
}