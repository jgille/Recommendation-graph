package rectest.index;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rectest.cache.Cache;
import rectest.cache.CacheBuilder;

public class SplitKeyKVStore<V> implements ReadOnlyKVStore<String, V> {

    private static class StringComparator implements Comparator<String> {
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }

    private static final int MAX_KEY_CACHE_SIZE = 100000;

    public static class Builder<V> {

        // TODO: Clear this once not needed anymore
        private final Cache<String, String> keyCache;

        private static final int DEFAULT_KEY_PART_LENGHT = 5;

        private final int keyPartLenght;
        private final Map<String, Pair<V, Builder<V>>> mapped =
            new TreeMap<String,  Pair<V, Builder<V>>>();

        public Builder(int averageKeyLength) {
            this(getKeyPartLenght(averageKeyLength),
                 createKeyCache(getKeyPartLenght(averageKeyLength)));

        }

        private Builder(int keyPartLenght, Cache<String, String> keyCache) {
            this.keyPartLenght = keyPartLenght;
            this.keyCache = keyCache;
        }

        private static Cache<String, String> createKeyCache(int keyPartLenght) {
            int size = Math.min((int)Math.pow(10, keyPartLenght), MAX_KEY_CACHE_SIZE);
            return new CacheBuilder<String, String>().maxSize(size).build();
        }

        private static int getKeyPartLenght(int averageKeyLength) {
            int length = Math.min((int)Math.ceil(1.0*averageKeyLength / 2), DEFAULT_KEY_PART_LENGHT);
            if (length < 0)
                return length;
            return DEFAULT_KEY_PART_LENGHT;
        }

        public synchronized Builder<V> put(String key, V value) {
                if (key.length() > keyPartLenght)
                    putChildBuilder(key, value);
                else
                    putValue(key, value);
                return this;
            }

        private void putChildBuilder(String key, V value) {
            String prefix = key.substring(0, keyPartLenght);
            String suffix = key.substring(keyPartLenght);
            Pair<V, Builder<V>> valueAndBuilder = mapped.get(prefix);
            if (valueAndBuilder == null) {
                valueAndBuilder = new Pair<V, Builder<V>>(null, new Builder<V>(keyPartLenght,
                                                                               keyCache));
                putValueAndBuilder(prefix, valueAndBuilder);
            }
            Builder<V> builder = valueAndBuilder.v2;
            if (builder == null) {
                builder = new Builder<V>(keyPartLenght, keyCache);
                valueAndBuilder.v2 = builder;
            }
            builder.put(suffix, value);
        }

        private void putValue(String key, V value) {
            Pair<V, Builder<V>> valueAndBuilder = mapped.get(key);
            if (valueAndBuilder == null) {
                valueAndBuilder = new Pair<V, Builder<V>>(value, null);
                putValueAndBuilder(key, valueAndBuilder);
                return;
            }
            valueAndBuilder.v1 = value;
        }

        private void putValueAndBuilder(String keyPart, Pair<V, Builder<V>> valueAndBuilder) {
            String key = keyCache.get(keyPart);
            if (key == null) {
                // new String to avoid some memory overhead created by substring
                key = new String(keyPart);
                keyCache.cache(key, key);
            }
            mapped.put(key, valueAndBuilder);
        }

        @SuppressWarnings ("unchecked")
        public synchronized SplitKeyKVStore<V> get() {
                List<String> keyParts = new ArrayList<String>();
                List<SplitKeyKVStore<V>> childStores = new ArrayList<SplitKeyKVStore<V>>();
                List<V> values = new ArrayList<V>();

                int nValues = 0;
                int nBuilders = 0;
                for(Map.Entry<String, Pair<V, Builder<V>>> e : mapped.entrySet()) {
                    String key = e.getKey();
                    Pair<V, Builder<V>> valueAndBuilder = e.getValue();
                    V value = valueAndBuilder.v1;
                    if(value != null)
                        nValues++;
                    Builder<V> childBuilder = valueAndBuilder.v2;
                    keyParts.add(key);
                    values.add(value);
                    if(childBuilder != null) {
                        childStores.add(childBuilder.get());
                        nBuilders++;
                    } else {
                        childStores.add(null);
                    }
                }
                SplitKeyKVStore<V>[] csArray = nBuilders > 0 ?
                    (SplitKeyKVStore<V>[])childStores.toArray(new SplitKeyKVStore[values.size()]) :
                    null;
                V[] valueArray =
                    nValues > 0 ? (V[])values.toArray(new Object[values.size()]) : null;

                mapped.clear();
                return new SplitKeyKVStore<V>(keyPartLenght,
                                              keyParts.toArray(new String[keyParts.size()]),
                                              csArray,
                                              valueArray);
            }

        private static class Pair<U, V> {
            private U v1;
            private V v2;
            public Pair(U v1, V v2) {
                this.v1 = v1;
                this.v2 = v2;
            }
        }
    }


    private static final Comparator<String> STRING_COMPARATOR = new StringComparator();

    private final int keyPartLenght;
    private final String[] keyParts;
    private final SplitKeyKVStore<V>[] childStores;
    private final V[] values;

    private SplitKeyKVStore(int keyPartLenght,
                            String[] keyParts,
                            SplitKeyKVStore<V>[] childStores,
                            V[] values) {
        this.keyPartLenght = keyPartLenght;
        this.keyParts = keyParts;
        this.childStores = childStores;
        this.values = values;
    }

    public V get(String key) {
        if (key.length() <= keyPartLenght)
            return findValue(key);
        return findChildValue(key);
    }

    private V findValue(String key) {
        if (values == null)
            return null;
        int index = findKeyIndex(key);
        if(index >= 0)
            return values[index];
        return null;
    }

    private V findChildValue(String key) {
        if (childStores == null)
            return null;
        String prefix = key.substring(0, keyPartLenght);
        String suffix = key.substring(keyPartLenght);
        int index = findKeyIndex(prefix);
        if(index < 0 || childStores[index] == null)
            return null;
        return childStores[index].get(suffix);
    }

    private int findKeyIndex(String keyPart) {
        return Arrays.binarySearch(keyParts, keyPart, STRING_COMPARATOR);
    }
}
