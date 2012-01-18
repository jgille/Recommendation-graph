package recng.db;

import gnu.trove.map.hash.TObjectLongHashMap;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBinKVStore implements KVStore<String, byte[]> {

    private static final String PREFIX = "#!_~*:";
    private static final String SUFFIX = "#!_~*!";

    private final byte[] prefix, suffix;

    private final TObjectLongHashMap<String> index;

    private final List<ByteSequence> shards = new ArrayList<ByteSequence>();

    public AbstractBinKVStore() {
        this.index = new TObjectLongHashMap<String>();
        try {
            this.prefix = getEntryPrefix().getBytes("UTF8");
            this.suffix = getEntrySuffix().getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected String getEntryPrefix() {
        return PREFIX;
    }

    protected String getEntrySuffix() {
        return SUFFIX;
    }

    @Override
    public void init(Map<String, String> properties) {
        // Ignore for now
    }

    @Override
    public byte[] get(String key) {
        long primaryKey;
        synchronized (index) {
            if (!index.contains(key))
                return null;
            primaryKey = index.get(key);
        }
        return getData(primaryKey);
    }

    @Override
    public void put(String key, byte[] value) {
        if (key == null)
            throw new IllegalArgumentException("Null keys not allowed");
        if (value == null) {
            remove(key);
            return;
        }
        put(key, false, value);
    }

    private void put(String key, boolean deleted, byte[] value) {
        Shard shard = getCurrentShard();
        ByteSequence shardData = shard.getData();
        int shardIndex = shard.getShardIndex();
        byte[] entry = createDataEntry(key, deleted, value);
        // TODO: Validate that the shard has room for the new entry
        int offset = shardData.append(entry);
        long primaryKey =
            createPrimaryKey(shardIndex, offset, entry.length);
        index.put(key, primaryKey);
    }

    private Shard getCurrentShard() {
        int size;
        synchronized (this) {
            size = shards.size();
        }
        if (size == 0) {
            ByteSequence shard = createStorage();
            synchronized (this) {
                shards.add(shard);
            }
            return new Shard(shard, 0);
        }
        synchronized (this) {
            return new Shard(shards.get(size - 1), size - 1);
        }
    }

    /**
     * Create a byte sequence used to store the data in.
     */
    protected abstract ByteSequence createStorage();

    protected synchronized int getShardCount() {
        return shards.size();
    }

    /**
     * Creates a data entry for a key/value pair to be stored.
     */
    private byte[] createDataEntry(String key, boolean deleted, byte[] value) {
        byte[] ka = serialize(key);
        if (ka.length > Short.MAX_VALUE)
            throw new IllegalArgumentException(
                                               "Max key length (2^15-1) exceeded");
        if (value.length > Integer.MAX_VALUE)
            throw new IllegalArgumentException(
                                               "Max value length (2^31-1) exceeded");

        int length = prefix.length; // prefix
        length += 1; // state flag
        length += 2; // key length
        length += ka.length; // key
        length += 4; // data length
        length += value.length; // data
        length += suffix.length; // suffix
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(prefix);
        buffer.put((byte) (deleted ? 0 : 1));
        buffer.putShort((short) ka.length);
        buffer.put(ka);
        buffer.putInt(value.length);
        buffer.put(value);
        buffer.put(suffix);
        return buffer.array();
    }

    private byte[] serialize(String s) {
        try {
            return s.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private byte[] getData(long primaryKey) {
        byte[] storedData = readData(primaryKey);
        ByteBuffer buffer = ByteBuffer.wrap(storedData);
        // The data contains:
        // - A constant prefix
        // - A 1 byte state flag (deleted or not)
        // - The key length
        // - The key
        // - The data length
        // - The data
        // - A constant suffix
        int position = prefix.length; // TODO: Validate prefix
        int state = buffer.get(position);
        if (state == 0)
            return null; // deleted
        position++;
        int keyLength = buffer.getShort(position);
        position += 2;
        position += keyLength;
        int dataLength = buffer.getInt(position);
        position += 4;
        byte[] data = new byte[dataLength];
        buffer.position(position);
        buffer.get(data);
        // TODO: Validate suffix
        return data;
    }

    @Override
    public byte[] remove(String key) {
        if (!index.contains(key))
            return null;
        long primaryKey = index.get(key);
        byte[] data = getData(primaryKey);
        // Append "deleted" entry
        put(key, true, new byte[0]);
        index.remove(key);
        return data;
    }

    private byte[] readData(long primaryKey) {
        int shardIndex = getShardIndex(primaryKey);
        if (shardIndex < 0 || shardIndex >= shards.size())
            return null;
        ByteSequence shard = shards.get(shardIndex);
        int offset = getOffset(primaryKey);
        int length = getLength(primaryKey);
        return shard.read(offset, length);
    }

    private static long createPrimaryKey(int shardIndex,
                                         int offset,
                                         int length) {
        long f = (shardIndex << 55); // First 9 bits represent shard index
        long o = (offset << 23); // Next 4 bytes is the offset into the shard
        long l = length; // The remaining 23 bits are the data length
        return f | o | l;
    }

    private static int getShardIndex(long primaryKey) {
        if (primaryKey < 0)
            return -1;
        return (int) (primaryKey >> 55); // 9 bits
    }

    private static int getOffset(long primaryKey) {
        if (primaryKey < 0)
            return -1;
        return (int) ((primaryKey >> 23) & 0xffffffff); // 32 bits
    }

    private static int getLength(long primaryKey) {
        if (primaryKey < 0)
            return -1;
        return (int) (primaryKey & 0x7fffff); // 23 bits
    }

    private static class Shard {
        private final ByteSequence data;
        private final int shardIndex;

        public Shard(ByteSequence data, int shardIndex) {
            this.data = data;
            this.shardIndex = shardIndex;
        }

        public ByteSequence getData() {
            return data;
        }

        public int getShardIndex() {
            return shardIndex;
        }
    }
}
