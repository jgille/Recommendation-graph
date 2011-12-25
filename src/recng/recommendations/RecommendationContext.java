package recng.recommendations;

import recng.graph.Graph;
import recng.graph.GraphBuilder;
import recng.graph.GraphImporter;
import recng.graph.GraphImporterImpl;
import recng.graph.GraphMetadata;
import recng.graph.MutableGraphImpl;
import recng.graph.NodeID;
import recng.graph.NodeType;
import recng.index.ID;
import recng.index.StringIDs;

public class RecommendationContext {

    private static RecommendationContext INSTANCE = null;
    private final RecommendationModel model;

    private RecommendationContext(RecommendationModel model) {
        this.model = model;
    }

    public RecommendationModel getModel() {
        return model;
    }

    public static synchronized RecommendationContext getInstance() {
        if (INSTANCE == null)
            throw new RuntimeException("Not set up");
        return INSTANCE;
    }

    public static synchronized void setup(String productGraphFile,
                                          ProductDataStore productData) {
        if (INSTANCE != null)
            throw new RuntimeException("Already set up");
        IDFactory<ID<String>> keyParser =
            new IDFactory<ID<String>>() {
                @Override
                public String toString(ID<String> productId) {
                    return productId.getID();
                }

                @Override
                public ID<String> fromString(String id) {
                    return StringIDs.parseKey(id);
                }
            };

        Graph<ID<String>> productGraph =
            importProductGraph(productGraphFile, keyParser);
        RecommendationModel model =
            new RecommendationModelImpl<ID<String>>(productGraph,
                                                     productData,
                                                     keyParser);
        INSTANCE = new RecommendationContext(model);
    }

    private static Graph<ID<String>>
        importProductGraph(String file, final IDFactory<ID<String>> keyParser) {
        GraphMetadata metadata = RecommendationGraphMetadata.getInstance();
        GraphBuilder<ID<String>> builder =
            new MutableGraphImpl.Builder<ID<String>>(metadata);
        GraphImporter<ID<String>> importer =
            new GraphImporterImpl<ID<String>>(builder, metadata) {

                @Override
                protected NodeID<ID<String>> getNodeKey(String id,
                                                        NodeType nodeType) {
                    return new NodeID<ID<String>>(keyParser.fromString(id),
                                                  nodeType);
                }
            };
        return importer.importGraph(file);
    }
}
