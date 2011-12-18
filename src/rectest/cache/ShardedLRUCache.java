package rectest.cache;

import java.util.ArrayList;
import java.util.List;

/**
 * A cache that uses multiple LRUCaches to store the values. This strategy might
 * improve performance in a highly concurrent environment because of a more
 * granular locking.
 *
 * Each key will be assigned to a shard (an LRUCache instance)
 * based on the modulo of the hashcode of the key.
 *
 * Note that this class does not guarantee strict LRU eviction because of
 * the sharding. Non uniformly distributed hashcode values for the keys
 * will result some shards evicting keys more often than others. In these
 * cases, consider overriding getShardIndex(int).
 *
 * This class is thread safe.
 *
 * @author Jon Ivmark
 */
class ShardedLRUCache<K, V> implements Cache<K, V> {

    private final List<LRUCache<K, V>> shards;
    private final int maxSizePerShard;

    ShardedLRUCache(int shardCount, int maxSizePerShard, long maxWeightPerShard,
                    Weigher<K, V> weigher) {
        if (shardCount < 1)
            throw new IllegalArgumentException("Shard count must be positive");
        this.maxSizePerShard = maxSizePerShard;
        this.shards = new ArrayList<LRUCache<K, V>>(shardCount);
        for (int i = 0; i < shardCount; i++)
            shards.add(new LRUCache<K, V>(maxSizePerShard, maxWeightPerShard, weigher));
    }

    /**
     * Returns the index of the shard a key recides on.
     *
     * Must return a value between 0 and shardCount - 1.
     */
    protected int getShardIndex(K key) {
        if(key == null)
            throw new IllegalArgumentException("The key may not be null");
        return Math.abs(key.hashCode() % shards.size());
    }

    private LRUCache<K, V> getShard(K key) {
        int index = getShardIndex(key);
        if (index < 0 || index >= shards.size())
            throw new IllegalArgumentException("Invalid shard index: " + index);
        return shards.get(index);
    }

    public V cache(K key, V value) {
        return getShard(key).cache(key, value);
    }

    public boolean contains(K key) {
        return getShard(key).contains(key);
    }

    public V evict(K key) {
        return getShard(key).evict(key);
    }

    public V get(K key) {
        return getShard(key).get(key);
    }

    public int size() {
        int size = 0;
        for (LRUCache<K, V> shard : shards)
            size += shard.size();
        return size;
    }

    public void clear() {
        for (Cache<K, V> cache : shards)
            cache.clear();
    }

    public int maxSize() {
        return maxSizePerShard * shards.size();
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Size: ").append(size()).append("\n\n");
        int i = 0;
        for (LRUCache<K, V> shard : shards) {
            sb.append(" *** Shard ").append(i++).append(" ***\n").
                append(shard.toString()).append("\n\n");
        }
        return sb.toString();
    }
}
