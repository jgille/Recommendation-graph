package recng.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Metadata for a table with a fixed set of fields.
 * 
 * @author Jon Ivmark
 */
public class TableMetadataImpl implements TableMetadata {
    private final Map<String, Integer> index = new HashMap<String, Integer>();
    private final List<FieldMetadata> fields = new ArrayList<FieldMetadata>();

    public TableMetadataImpl(List<FieldMetadata> fieldMetadata) {
        int i = 0;
        for (FieldMetadata field : fieldMetadata) {
            index.put(field.getFieldName(), i++);
            fields.add(field);
        }
    }

    @Override
    public FieldMetadata getFieldMetadata(String fieldName) {
        if (!index.containsKey(fieldName))
            return null;
        int ordinal = index.get(fieldName);
        return getFieldMetadata(ordinal);
    }

    @Override
    public FieldMetadata getFieldMetadata(int ordinal) {
        if (ordinal < 0 || ordinal >= fields.size())
            throw new IllegalArgumentException("Invalid ordinal: " + ordinal);
        return fields.get(ordinal);
    }

    @Override
    public boolean contains(String fieldName) {
        return index.containsKey(fieldName);
    }

    @Override
    public List<String> getFields() {
        List<String> res = new ArrayList<String>();
        for (FieldMetadata fm : fields)
            res.add(fm.getFieldName());
        return res;
    }

    /**
     * The ordinal will be the position at which this field was created,
     * insertion order is kept.
     */
    @Override
    public int ordinal(String fieldName) {
        if (!contains(fieldName))
            throw new IllegalArgumentException("Unknown field name: "
                + fieldName);
        return index.get(fieldName);
    }

    @Override
    public int size() {
        return index.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (FieldMetadata field : fields)
            sb.append("\n").append(field);
        sb.append("\n]");
        return sb.toString();
    }
}
