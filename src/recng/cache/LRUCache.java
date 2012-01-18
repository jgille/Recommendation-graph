package recng.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A LRU cache based on a LinkedHashMap.
 *
 * This class is thread safe.
 *
 * @author Jon Ivmark
 */
public class LRUCache<K, V> implements Cache<K, V> {

    private static final float LOAD_FACTOR = 1f;

    private final AtomicInteger weight = new AtomicInteger(0);
    private final int maxSize;
    private final Map<K, V> cache;
    private final long maxWeight;
    private final Weigher<K, V> weigher;

    /**
     * Approximate memory overhead (in bytes) for each cached key/value pair.
     */
    private static final int ENTRY_OVERHEAD = 40;

    /**
     * Constructs a new cache.
     *
     * @param maxSize
     *            The maximum number of elements in the cache.
     * @param maxWeight
     *            The maximum total weight, sized in bytes, of the cache.
     * @param weigher
     *            Used to weigh, i.e. estimate size in bytes, the ey/value pairs
     */
    LRUCache(final int maxSize, final long maxWeight, Weigher<K, V> weigher) {
        this.maxSize = maxSize;
        this.maxWeight = maxWeight;
        this.weigher = weigher;
        Map<K, V> underlying = new LinkedHashMap<K, V>(maxSize + 1, LOAD_FACTOR, true) {
                private static final long serialVersionUID = 201212231211L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    if ((maxSize >= 0 && size() > maxSize) ||
                        (maxWeight >= 0 && getWeight() > maxWeight)) {
                        evict(eldest.getKey());
                    }
                    // Note: Always return false here since we modify the map
                    // ourselves through evict
                    return false;
                }
        };
        this.cache = Collections.synchronizedMap(underlying);
    }

    public V cache(K key, V value) {
        if(key == null)
            throw new IllegalArgumentException("Null key not allowed");
        if (weigher != null) {
            if (cache.containsKey(key)) {
                V prev = cache.get(key);
                weight.addAndGet(-weigh(key, prev));
            }
            weight.addAndGet(weigh(key, value));
        }
        return cache.put(key, value);
    }

    public boolean contains(K key) {
        return cache.containsKey(key);
    }

    public V evict(K key) {
        V prev = cache.remove(key);
        if (prev != null)
            weight.addAndGet(-weigh(key, prev));
        return prev;
    }

    public V get(K key) {
        return cache.get(key);
    }

    public int size() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }

    public int maxSize() {
        return maxSize;
    }

    public long maxWeight() {
        return maxWeight;
    }

    public long getWeight() {
        return weight.get();
    }

    private int weigh(K key, V value) {
        if (weigher == null)
            return 0;
        return weigher.weigh(ENTRY_OVERHEAD, key, value);
    }

    @Override public String toString() {
        return String.format("{maxSize: %s,\n size: %s,\n maxWeight: %s,\n weight: %s}",
                             maxSize, cache.size(), maxWeight, getWeight());
    }
}
