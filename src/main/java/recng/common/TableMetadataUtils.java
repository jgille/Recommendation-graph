package recng.common;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Utility methods for creating {@link TableMetadata} instances.
 *
 * @author jon
 *
 */
public class TableMetadataUtils {

    private static final Pattern METADATA_PATTERN = Pattern
        .compile("(\\S*)\\s*(\\S*)");

    /**
     * Parses table metadata from file (csv style).
     */
    public static TableMetadata parseTableMetadata(String file)
        throws IOException {
        if (file.endsWith(".gz"))
            return parseGZippedTableMetadata(file);
        BufferedReader br = null;
        List<FieldMetadata> fields;
        try {
            br = new BufferedReader(new FileReader(file));
            fields = readFields(br);
        } finally {
            if (br != null)
                br.close();
        }
        return new TableMetadataImpl(fields);
    }

    private static TableMetadata parseGZippedTableMetadata(String file)
        throws IOException {
        List<FieldMetadata> fields;
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = new GZIPInputStream(new FileInputStream(file));
            reader = new BufferedReader(new InputStreamReader(is));
            fields = readFields(reader);
        } finally {
            close(is);
            close(reader);
        }
        return new TableMetadataImpl(fields);
    }

    private static List<FieldMetadata> readFields(BufferedReader reader)
        throws IOException {
        List<FieldMetadata> fields = new ArrayList<FieldMetadata>();
        fields.add(FieldMetadata.ID);
        String line = null;
        while ((line = reader.readLine()) != null) {
            Matcher m = METADATA_PATTERN.matcher(line);
            if (!m.matches())
                continue;
            String fieldName = m.group(1);
            String typeName = m.group(2).toUpperCase();
            FieldMetadata.Type type = FieldMetadata.Type.valueOf(typeName);
            fields.add(getFieldMetadata(fieldName, type));
        }
        return fields;
    }

    private static FieldMetadata getFieldMetadata(String fieldName,
                                                  FieldMetadata.Type type) {
        boolean required = isRequired(fieldName);
        Object defaultValue = getDefaultValue(fieldName, type);
        return new FieldMetadataImpl.Builder(fieldName, type)
            .setRequired(required).setDefaultValue(defaultValue).build();
    }

    private static Object getDefaultValue(String fieldName,
                                          FieldMetadata.Type type) {
        return null; // TODO: Implement
    }

    private static boolean isRequired(String fieldName) {
        return false; // TODO: Implement
    }

    private static void close(Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
