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
    private final Map<String, FM> field2FieldMetadata;
    private final Map<Integer, FieldMetadata<?>> ordinal2FieldMetadata;

    private TableMetadataImpl(Map<String, FM> fields) {
        this.field2FieldMetadata = Collections.unmodifiableMap(fields);
        this.ordinal2FieldMetadata = new HashMap<Integer, FieldMetadata<?>>();
        for (FM fm : fields.values())
            ordinal2FieldMetadata.put(fm.ordinal, fm.metadata);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> FieldMetadata<T> getFieldMetadata(String fieldName) {
        return (FieldMetadata<T>) getFM(fieldName).getFieldMetadata();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> FieldMetadata<T> getFieldMetadata(int ordinal) {
        return (FieldMetadata<T>) ordinal2FieldMetadata.get(ordinal);
    }

    @Override
    public boolean contains(String fieldName) {
        return field2FieldMetadata.containsKey(fieldName);
    }

    @Override
    public FieldMetadata.Type typeOf(String fieldName) {
        return getFM(fieldName).getFieldMetadata().getType();
    }

    @Override
    public Set<String> getFields() {
        return new HashSet<String>(field2FieldMetadata.keySet());
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
        return getFM(fieldName).ordinal();
    }

    private FM getFM(String fieldName) {
        if (!contains(fieldName))
            throw new IllegalArgumentException("Unknown field name: "
                + fieldName);
        return field2FieldMetadata.get(fieldName);
    }

    @Override
    public int size() {
        return field2FieldMetadata.size();
    }

    /**
     * Util class keeping track of a {@link FieldMetadata} instance and it's
     * ordinal.
     *
     */
    private static class FM {
        private final FieldMetadata<?> metadata;
        private final int ordinal;

        public FM(FieldMetadata<?> metadata, int ordinal) {
            this.metadata = metadata;
            this.ordinal = ordinal;
        }

        public FieldMetadata<?> getFieldMetadata() {
            return metadata;
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
