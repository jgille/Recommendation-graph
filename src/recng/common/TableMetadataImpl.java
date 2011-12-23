package recng.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Metadata for a fixed set of fields.
 *
 * @author Jon Ivmark
 */
public class TableMetadataImpl implements TableMetadata {
    private final Map<String, FM> fields;

    private TableMetadataImpl(Map<String, FM> fields) {
        this.fields = Collections.unmodifiableMap(fields);
    }

    @SuppressWarnings("unchecked")
    public <T> FieldMetadata<T> getFieldMetadata(String fieldName) {
        return (FieldMetadata<T>) getFM(fieldName).getFieldMetadata();
    }

    public boolean contains(String fieldName) {
        return fields.containsKey(fieldName);
    }

    public FieldMetadata.Type typeOf(String fieldName) {
        return getFM(fieldName).getFieldMetadata().getType();
    }

    public Set<String> getFields() {
        return new HashSet<String>(fields.keySet());
    }

    /**
     * The ordinal will be the position at which this field was created,
     * insertion order is kept.
     */
    public int ordinal(String fieldName) {
        if (!contains(fieldName))
            throw new IllegalArgumentException("Unknown field name: "
                + fieldName);
        return getFM(fieldName).ordinal();
    }

    private FM getFM(String fieldName) {
        if (!contains(fieldName))
            throw new IllegalArgumentException("Unknown field name: "
                + fieldName);
        return fields.get(fieldName);
    }

    public int size() {
        return fields.size();
    }

    /**
     * Util class keeping track of a {@link FieldMetadata} instance and it's
     * ordinal.
     * 
     */
    private static class FM {
        private final FieldMetadata<?> fm;
        private final int ordinal;

        public FM(FieldMetadata<?> fm, int ordinal) {
            this.fm = fm;
            this.ordinal = ordinal;
        }

        public FieldMetadata<?> getFieldMetadata() {
            return fm;
        }

        public int ordinal() {
            return ordinal;
        }
    }

    /**
     * A class used to build a {@link TableMetadata} instance.
     *
     * @author jon
     */
    public static class Builder {
        private final Map<String, FM> fields = new HashMap<String, FM>();
        private int currentOrdinal = 0;
        private boolean built = false;

        public synchronized Builder addAll(TableMetadata fs) {
            for (String name : fs.getFields())
                add(fs.getFieldMetadata(name));
            return this;
        }

        public synchronized Builder add(FieldMetadata<?> fm) {
            String fieldName = fm.getFieldName();
            if(fields.containsKey(fieldName))
                throw new IllegalArgumentException(fieldName + " has already been added.");
            if(built)
                throw new IllegalStateException("This builder has already been used " +
                                                "to build a field metadata instance");
            fields.put(fieldName, new FM(fm, currentOrdinal++));
            return this;
        }

        public synchronized boolean containsField(String fieldName) {
            return fields.containsKey(fieldName);
        }

        public synchronized TableMetadata build() {
            built = true;
            return new TableMetadataImpl(fields);
        }
    }
}
