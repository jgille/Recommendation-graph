package recng.common;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A property container (key/value store) with a fixed set of keys.
 * 
 * The data is stored in a single byte array, another array contains indexes
 * keeping track of which bytes belong to which field. Marshallers able to
 * translate the byte arrays into objects and vice versa are linked to each
 * field.
 * 
 * NOTE: All methods in this class may throw an IllegalArgumentException or
 * ClassCastException if the type of an object does not match that of the field
 * described in the TableMetadata for this container.
 * 
 * Repeated fields, i.e. lists, are stored in the same data array as the
 * primitive fields. Each repeated value if prepended with 2 bytes that
 * represents the size of the field in bytes.
 * 
 * NOTE: This class can hold at most 2^31-1 bytes of data, or around 2G. Trying
 * to add more data will result in an exception. Individual values have a max
 * size of 2^15-1 bytes, around 32K.
 * 
 * This class is thread safe.
 * 
 * @author Jon Ivmark
 */
public class BinPropertyContainer implements WeightedPropertyContainer {

    private final TableMetadata fields;
    private byte[] data = null;
    protected long[] indexes;
    private final boolean useSparseIndexing;

    /**
     * @param fields
     *            The fields that this container can contain
     * @param useSparseIndexing
     *            If true, fields with no corresponding value are never put in
     *            the index. This saves space, but can lead to a slight
     *            performance degradation for both reads and writes.
     */
    private BinPropertyContainer(TableMetadata fields, boolean useSparseIndexing) {
        this.fields = fields;
        this.useSparseIndexing = useSparseIndexing;
        if (!useSparseIndexing) {
            this.indexes = new long[fields.size()];
            Arrays.fill(indexes, -1);
        } else {
            this.indexes = new long[0];
        }
    }

    public void mergeWith(Map<String, Object> properties) {
        for (Map.Entry<String, Object> prop : properties.entrySet()) {
            setProperty(prop.getKey(), prop.getValue());
        }
    }

    /**
     * Gets the approximate size of this container in bytes.
     */
    @Override
    public synchronized int getWeight() {
        int weight = 8; // Object overhead
        weight += 4; // Reference to the FieldSet
        weight += indexes.length * 8 + 12; // each long in the index + the array
                                           // overhead
        if (data != null) {
            weight += 12; // array overhead
            weight += data.length; // the actual data
        }
        return weight;
    }

    @Override
    public synchronized Object getProperty(String fieldName) {
        if (!fields.contains(fieldName))
            throw new IllegalArgumentException("Unrecognized field: "
                + fieldName);
        if (fields.getFieldMetadata(fieldName).isRepeated())
            return getRepeatedProperties(fieldName);
        byte[] ba = getBytes(fieldName);
        if (ba == null)
            return null;
        Marshaller marshaller = getMarshaller(fieldName);
        return marshaller.unmarshall(ba);
    }

    private byte[] getBytes(String fieldName) {
        // Get the index for this field
        long index = getIndex(fieldName);
        // Get the offset in the data array
        int offset = getOffset(index);
        // Get the lenght of the corresponding value
        int length = getLength(index);
        if (offset < 0 || length < 1)
            return null;
        // Return the corresponding range of the data array
        return Arrays.copyOfRange(data, offset, offset + length);
    }

    /*
     * Searches for a field in the sorted index array.
     */
    private static int binarySearch(long[] indexes, int fieldOrdinal) {
        int low = 0;
        int high = indexes.length - 1;
        int mid;

        while (low <= high) {
            // Use shift instead of division to avoid the risk of overflow
            mid = (low + high) >>> 1;
            int midOrdinal = getFieldOrdinal(indexes[mid]);
            if (midOrdinal < fieldOrdinal)
                low = mid + 1;
            else if (midOrdinal > fieldOrdinal)
                high = mid - 1;
            else
                return mid;
        }
        return -1;
    }

    /*
     * Gets the corresponding index for a field.
     */
    private long getIndex(String fieldName) {
        if (useSparseIndexing) {
            int ordinal = fields.ordinal(fieldName);
            int index = binarySearch(indexes, ordinal);
            if (index > -1)
                return indexes[index];
            return -1;
        }

        // In the non sparse implementation indexes are stored in the array
        // placed according to their ordinals
        int ordinal = fields.ordinal(fieldName);
        if (ordinal < 0 || ordinal >= indexes.length)
            throw new IndexOutOfBoundsException("Unable to get value for field " +
                fieldName + ", ordinal = "
                + ordinal);
        long index = indexes[ordinal];
        return index;
    }

    /*
     * Inserts an index for a field.
     */
    private void index(String fieldName, long index) {
        if (useSparseIndexing) {
            // Search for the field
            int i = binarySearch(indexes, fields.ordinal(fieldName));
            if (i >= 0) { // If found, update it's index
                indexes[i] = index;
                return;
            }
            // Else expand index array and add a new index
            indexes = Arrays.copyOfRange(indexes, 0, indexes.length + 1);
            indexes[indexes.length - 1] = index;
            Arrays.sort(indexes); // Keep the indexes sorted on field ordinal
            return;
        }
        int ordinal = fields.ordinal(fieldName);
        if (ordinal < 0 || ordinal >= indexes.length)
            throw new IndexOutOfBoundsException("Unable to get value for field " +
                fieldName + ", ordinal = "
                + ordinal);
        indexes[ordinal] = index;
    }

    /*
     * The ordinal is the first 2 bytes of the index.
     */
    private static int getFieldOrdinal(long index) {
        if (index < 0)
            return -1;
        // Shift out the trailing 48 bits, leaving the 16 bits identifying the
        // field
        int ordinal = (int) (index >> 48);
        return ordinal;
    }

    /**
     * Gets the length of a field's value (in bytes).
     * 
     * The lenght is the last 2 bytes of the index.
     */
    protected int getLength(long index) {
        if (index < 0)
            return -1;
        // Zero out everything but the lenght
        int length = (int) (index & 0xffff);
        return length;
    }

    /**
     * Gets the offset in the data array for a field.
     * 
     * The offset is the middle 4 bytes of the index.
     */
    protected int getOffset(long index) {
        if (index <= 0)
            return -1;
        index = index << 16; // Shift out field identifier
        index = index >> 32; // Put offset bytes (32 bits) last
        int offset = (int) index;
        return offset;
    }

    /*
     * Creates a long where:
     * - The first 2 bytes identifies the field (it's ordinal).
     * - The next 4 bytes contains the offset in the data array for this field.
     * - The final 2 bytes contains the lenght of the field value (in bytes).
     */
    private long createIndex(String fieldName, int offset, int length) {
        long f = (long) fields.ordinal(fieldName) << 48;
        long o = (long) offset << 16;
        long l = length;
        long index = f | o | l;
        return index;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized Object setProperty(String fieldName, Object value) {
        if (!fields.contains(fieldName))
            throw new IllegalArgumentException("Unrecognized field: "
                + fieldName);
        if (fields.getFieldMetadata(fieldName).isRepeated()) {
            if (value instanceof List)
                return setRepeatedProperties(fieldName, (List<Object>) value);
            return setRepeatedProperties(fieldName,
                                         Collections.singletonList(value));
        }

        Marshaller marshaller = getMarshaller(fieldName);
        // Get the previously stored value for this field, if any
        byte[] prev = getBytes(fieldName);
        // Marshall a byte array to be stored
        byte[] bytes = value == null ? null : marshaller.marshall(value);
        store(fieldName, bytes);
        if (prev == null)
            return null;
        return marshaller.unmarshall(prev);
    }

    private void store(String fieldName, byte[] bytes) {
        // Get the index for this field
        long index = getIndex(fieldName);
        int offset = getOffset(index);
        int length = getLength(index);
        if (offset >= 0) {
            // This means we are updating an existing key/value pair.
            // Start by clearing out it's value
            Arrays.fill(data, offset, offset + length, (byte) 0);
            if (bytes != null) {
                if (length >= bytes.length) { // The new value fits in the old
                                              // location
                    inplaceUpdate(fieldName, offset, bytes);
                } else {
                    // The data does not fit and needs to be put last in the
                    // array
                    appendData(fieldName, bytes);
                }
            }
        } else {
            // This is a new key/value pair. Append it.
            if (bytes != null)
                appendData(fieldName, bytes);
        }

        if (bytes == null)
            remove(fieldName);
    }

    @Override
    public synchronized List<Object> getRepeatedProperties(String fieldName) {
        if (!fields.contains(fieldName))
            throw new IllegalArgumentException("Unrecognized field: "
                + fieldName);
        byte[] data = getBytes(fieldName);
        return fromBytes(fieldName, data);
    }

    /**
     * Sets the values for a repeated property field.
     * 
     * NOTE: Individual repeated field values has a maximum size of 2^15 - 1
     * bytes.
     */
    @Override
    public synchronized List<Object> setRepeatedProperties(String fieldName,
                                                           List<Object> values) {
        if (!fields.contains(fieldName))
            throw new IllegalArgumentException("Unrecognized field: "
                + fieldName);
        // Get the previously stored value for this field, if any
        byte[] prev = getBytes(fieldName);
        byte[] bytes = toBytes(fieldName, values);
        store(fieldName, bytes);
        return fromBytes(fieldName, prev);
    }

    /**
     * Add a a repeated property.
     * 
     * NOTE: Individual repeated field values has a maximum size of 2^15 - 1
     * bytes.
     */
    @Override
    public synchronized void
        addRepeatedProperty(String fieldName, Object value) {
        byte[] data = getBytes(fieldName);
        List<Object> properties = fromBytes(fieldName, data);
        if (properties == null)
            properties = new ArrayList<Object>();
        properties.add(value);
        store(fieldName, toBytes(fieldName, properties));
    }

    /**
     * Repeated values, i.e. lists of values, are stored in the byte array as
     * follows: For each repeated value we store 2 bytes (a short) that
     * represents the size (number of bytes) of the field, followed by the
     * actual data.
     * 
     * NOTE: This means that individual repeated fields has a maximum length of
     * 2^15 - 1 bytes.
     */
    private byte[] toBytes(String fieldName, List<?> values) {
        if (values == null)
            return null;
        Marshaller marshaller = getMarshaller(fieldName);
        byte[][] data = new byte[values.size()][];
        int dataSize = 0;
        int i = 0;
        for (Object value : values) {
            byte[] bytes = marshaller.marshall(value);
            if (bytes == null)
                continue;
            dataSize += bytes.length;
            data[i++] = bytes;
        }
        ByteBuffer buffer = ByteBuffer.allocate(dataSize + 2 * i);
        for (byte[] bytes : data) {
            short length = (short) bytes.length;
            buffer.putShort(length);
            buffer.put(bytes);
        }
        return buffer.array();
    }

    /*
     * Marshalls a byte array into a list of objects.
     */
    private List<Object> fromBytes(String fieldName, byte[] data) {
        if (data == null)
            return null;
        int i = 0;
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Marshaller marshaller = getMarshaller(fieldName);
        List<Object> res = new ArrayList<Object>();
        while (i < data.length - 1) {
            short length = buffer.getShort(i);
            i += 2; // The length
            byte[] bytes = Arrays.copyOfRange(data, i, i + length);
            Object value = marshaller.unmarshall(bytes);
            i += length; // The data
            res.add(value);
        }
        return res;
    }

    private void remove(String fieldName) {
        index(fieldName, createIndex(fieldName, -1, -1)); // Removes it from the
                                                          // index
    }

    /**
     * Updates a range of the data array.
     */
    private void inplaceUpdate(String fieldName, int offset, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++)
            data[offset + i] = bytes[i];
        long offsetAndLenght = createIndex(fieldName, offset, bytes.length);
        index(fieldName, offsetAndLenght);
    }

    /**
     * Appends bytes to the data array.
     */
    private void appendData(String fieldName, byte[] bytes) {
        checkCapacity(bytes);
        if (bytes == null)
            throw new IllegalArgumentException("Can not append null byte array");
        int offset;
        if (data == null) {
            data = new byte[bytes.length];
            offset = 0;
        } else {
            offset = data.length;
            data = Arrays.copyOf(data, data.length + bytes.length);
        }
        for (int i = 0; i < bytes.length; i++)
            data[offset + i] = bytes[i];
        long offsetAndLenght = createIndex(fieldName, offset, bytes.length);
        index(fieldName, offsetAndLenght);
    }

    private void checkCapacity(byte[] bytes) {
        int valueSize = bytes != null ? bytes.length : 0;
        int dataSize = data != null ? data.length : 0;
        if (valueSize > Short.MAX_VALUE) // Max size = 2^15-1
            throw new IllegalArgumentException("The value is too large: "
                + valueSize);
        if (valueSize + (long) dataSize > Integer.MAX_VALUE) // Max total size =
                                                             // 2^31-1
            throw new IllegalArgumentException("Capacity exceeded, data size: "
                + dataSize + ", value size: " + valueSize);
    }

    private Marshaller getMarshaller(String fieldName) {
        FieldMetadata fm = fields.getFieldMetadata(fieldName);
        if (fm == null)
            return null;
        return fm.getMarshaller();
    }

    @Override
    public synchronized boolean containsProperty(String fieldName) {
        if (!fields.contains(fieldName))
            return false;
        long index = getIndex(fieldName);
        return getOffset(index) >= 0 && getLength(index) > 0;
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<String, Object>(fields.size());
        for (String fieldName : fields.getFields()) {
            Object property = getProperty(fieldName);
            if (property != null)
                map.put(fieldName, property);
        }
        return map;
    }

    public List<String> getKeys() {
        return fields.getFields();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : getKeys())
            sb.append(key).append(" : ").append(getProperty(key)).append(", ");
        return sb.toString();
    }

    public static class Factory implements PropertyContainerFactory {

        private final boolean useSparseIndexing;

        public Factory() {
            this(false);
        }

        public Factory(boolean useSparseIndexing) {
            this.useSparseIndexing = useSparseIndexing;
        }

        /**
         * This method is not supported and will throw a
         * {@link NoSuchMethodError}.
         */
        @Override
        public BinPropertyContainer create() {
            throw new NoSuchMethodError("This class requires metadata," +
                "use create(TableMetadata)");
        }

        @Override
        public BinPropertyContainer create(TableMetadata metadata) {
            if (metadata == null)
                throw new IllegalArgumentException(
                                                   "This class requires metadata");
            return new BinPropertyContainer(metadata, useSparseIndexing);
        }
    }
}
