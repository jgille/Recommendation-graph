package rectest.cache;

/**
 * A class used to build cache instances.
 *
 * This class is thread safe.
 *
 * @author Jon Ivmark
 */
public class CacheBuilder<K, V> {
    private int concurrencyLevel = 4;
    private int maxSize = -1;
    private long maxWeight = -1;
    private Weigher<K, V> weigher = null;

    /**
     * Specifies the maximum number of entries that the cache should contain.
     * When the limit is reached, caching new entries will lead to other LRU entries
     * being evicted.
     *
     * Note: With a concurrencyLevel greater than 1, the cache is internally partitioned and
     * as an effect entries might be evicted before the total maximum size has been reached
     * (each partition will have it's own maximum size). With a well distributes hashCode of
     * the keys in the cache, this should not present a problem.
     *
     * Defaults to -1, meaning no maximum size.
     */
    public synchronized CacheBuilder<K, V> maxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /**
     * Specifies the maximum total weight of the entries in the cache. Weights can be of arbitrary
     * units. For this method to work, CacheBuilder.weigher() needs to have been called.
     *
     * Once the total weight surpasses the maximum weight, caching new entries will lead to other
     * LRU entries being evicted.
     *
     * This method can be useful if you want to have a cache with an (approximate) maximum
     * memory footprint.
     *
     * Note: With a concurrencyLevel greater than 1, the cache is internally partitioned and
     * as an effect entries might be evicted before the total maximum weight has been reached
     * (each partition will have it's own maximum weight). With a well distributes hashCode of
     * the keys in the cache, this should not present a problem.
     *
     * Defaults to -1, meaning no maximum weight.
     */
    public synchronized CacheBuilder<K, V> maxWeight(long maxWeight) {
        this.maxWeight = maxWeight;
        return this;
    }

    /**
     * Specifies a weigher instance used to weigh entries in the cache. Used together with
     * CacheBuilder.maxWeight.
     *
     * Defaults to null.
     */
    public synchronized CacheBuilder<K, V> weigher(Weigher<K, V> weigher) {
    	this.weigher = weigher;
    	return this;
    }

    /**
     * Specifies the expected number of threads that will concurrently read from/write to
     * the cache. The cache is internally partitioned to allow multiple concurrent operations
     * on it, and an accurate guidance on how many partitions to use will improve concurrency.
     *
     * Defaults to 4.
     */
    public synchronized CacheBuilder<K, V> concurrencyLevel(int concurrencyLevel) {
        this.concurrencyLevel = Math.max(1, concurrencyLevel);
        return this;
    }
    
    /**
     * Builds a cache instance using the settings from this builder.
     */
    public synchronized Cache<K, V> build() {
        if (concurrencyLevel == 1)
            return new LRUCache<K, V>(maxSize, maxWeight, weigher);
        int maxSizePerShard = maxSize > 0 ? (int)Math.ceil(1d*maxSize/concurrencyLevel) : maxSize;
        long maxWeightPerShard = maxWeight > 0 ?
            (int)Math.ceil(1d*maxWeight/concurrencyLevel) : maxWeight;
        return new ShardedLRUCache<K, V>(concurrencyLevel, maxSizePerShard, maxWeightPerShard,
                                   		weigher);
    }
}
