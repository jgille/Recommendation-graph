package recng.recommendations;

import recng.cache.CacheBuilder;
import recng.cache.Weigher;
import recng.graph.Graph;
import recng.index.Key;
import recng.index.StringKeys;

public class RecommendationContext {

    private static RecommendationContext INSTANCE = null;
    private final RecommendationModel<Key<String>> model;

    private RecommendationContext(RecommendationModel<Key<String>> model) {
        this.model = model;
    }

    public RecommendationModel<Key<String>> getModel() {
        return model;
    }

    public static synchronized RecommendationContext getInstance() {
        if (INSTANCE == null)
            throw new RuntimeException("Not set up");
        return INSTANCE;
    }

    public static synchronized void setup(String npDataFile,
                                          String clickDataFile,
                                          ProductData productMetadata) {
        if (INSTANCE != null)
            throw new RuntimeException("Already set up");
        KeyParser<Key<String>> pip =
            new KeyParser<Key<String>>() {
                @Override
                public String toString(Key<String> productId) {
                    return productId.getValue();
                }

                @Override
                public Key<String> parseKey(String id) {
                    return StringKeys.parseKey(id);
                }
            };

        Graph<Key<String>> productGraph =
            buildProductGraph(npDataFile, clickDataFile, pip);
        ProductCache<Key<String>> productMetadataCache =
            setupCache(productGraph.nodeCount());
        RecommendationModel<Key<String>> model =
            new RecommendationModelImpl<Key<String>>(productGraph,
                                                     productMetadata,
                                                     productMetadataCache,
                                                     pip);
        INSTANCE = new RecommendationContext(model);
    }

    private static Graph<Key<String>>
        buildProductGraph(String npDataFile,
                          String clickDataFile,
                          KeyParser<Key<String>> pip) {
        return new PredictorImpl().setupPredictions(npDataFile, clickDataFile,
                                                    pip);
    }

    private static ProductCache<Key<String>>
        setupCache(int numberOfProducts) {
        CacheBuilder<Key<String>, Product<Key<String>>> builder =
            new CacheBuilder<Key<String>, Product<Key<String>>>();

        builder.weigher(new Weigher<Key<String>, Product<Key<String>>>() {
            @Override
            public int weigh(int overhead, Key<String> key, Product<Key<String>> value) {
                return overhead +
                    40 + //  estimated key size
                    value.getWeight();
            }
        });
        builder.maxWeight(Runtime.getRuntime().maxMemory() / 4);
        builder.maxSize(numberOfProducts);

        return new ProductCacheImpl<Key<String>>(builder.build());
    }
}
