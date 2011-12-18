package rectest.recommendations;

import rectest.cache.CacheBuilder;
import rectest.cache.Weigher;
import rectest.graph.Graph;
import rectest.index.Key;
import rectest.index.StringKeys;

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
                                          ProductMetadata productMetadata) {
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
        ProductMetadataCache<Key<String>> productMetadataCache =
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

    private static ProductMetadataCache<Key<String>>
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

        return new ProductMetadataCacheImpl<Key<String>>(builder.build());
    }
}