package recng.recommendations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import recng.common.BinPropertyContainer;
import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.PropertyContainer;
import recng.common.PropertyContainerFactory;
import recng.common.TableMetadata;
import recng.common.TableMetadataImpl;
import recng.common.TableMetadataUtils;
import recng.common.io.CSVDescriptor;
import recng.common.io.CSVPropertyCursor;
import recng.common.io.CSVUtils;
import recng.db.InMemoryKVStore;
import recng.db.KVStore;
import recng.graph.Graph;
import recng.graph.GraphBuilder;
import recng.graph.GraphImporter;
import recng.graph.GraphImporterImpl;
import recng.graph.GraphMetadata;
import recng.graph.ImmutableGraphImpl;
import recng.index.ID;
import recng.index.StringIDs;
import recng.index.StringToStringIdConverter;
import recng.recommendations.data.ProductRepository;
import recng.recommendations.domain.Product;
import recng.recommendations.graph.RecommendationGraphMetadata;

/**
 * Service for graph based recommendations.
 *
 * @author jon
 *
 */
public class RecommendationService {

    private final RecommendationModel model;

    public RecommendationService(RecommendationModel model) {
        this.model = model;
    }

    public RecommendationModel getModel() {
        return model;
    }

    /**
     * Sets up an all RAM recommendation model, i.e. graph + product data in
     * RAM.
     *
     * @param graphFile
     *            An exported graph.
     * @param productDataFile
     *            The product data file.
     * @param productFormatFile
     *            The product format file.
     */
    public static RecommendationService setup(String graphFile,
                                              String productDataFile, String productFormatFile)
        throws IOException {
        GraphMetadata graphMetadata = RecommendationGraphMetadata.getInstance();

        // HERE

        GraphBuilder<ID<String>> builder = ImmutableGraphImpl.Builder
            .create(graphMetadata);
        GraphImporter<ID<String>> importer =
            new GraphImporterImpl<ID<String>>(
                                              builder, graphMetadata,
                                              new StringToStringIdConverter());

        Graph<ID<String>> graph = importer.importGraph(graphFile);
        TableMetadata metadata = TableMetadataUtils
            .parseTableMetadata(productFormatFile);

        final KVStore<ID<String>, PropertyContainer> productData =
            new InMemoryKVStore<ID<String>, PropertyContainer>();
        CSVDescriptor descriptor = new CSVDescriptor();
        descriptor.setGzipped(productDataFile.endsWith(".gz")).setMetadata(
                                                                           metadata);
        PropertyContainerFactory factory = new BinPropertyContainer.Factory();
        CSVPropertyCursor cursor = CSVUtils.readAndParse(productDataFile,
                                                         descriptor, factory);
        try {
            PropertyContainer props;
            int j = 0;
            while ((props = cursor.nextRow()) != null) {
                String pid = (String) props.getProperty(FieldMetadata.ID
                    .getFieldName());
                productData.put(StringIDs.parseID(pid), props);
                if (++j % 10000 == 0) {
                    System.out.println(String.format("Uploaded %s products..",
                                                     j));
                }
            }
        } finally {
            cursor.close();
        }

        List<FieldMetadata> fieldMetadata = new ArrayList<FieldMetadata>();
        for (String fieldName : metadata.getFields()) {
            fieldMetadata.add(metadata.getFieldMetadata(fieldName));
        }
        fieldMetadata.add(FieldMetadataImpl.create(Product.IS_VALID_PROPERTY,
                                                   FieldMetadata.Type.BOOLEAN));
        final IDParser<ID<String>> idFactory = new StringIDParser();
        final TableMetadata completeMetadata = new TableMetadataImpl(
                                                                     fieldMetadata);
        ProductRepository productRepo = new ProductRepository() {

            @Override
            public TableMetadata getMetadata() {
                return completeMetadata;
            }

            @Override
            public Map<String, Object> getProductData(String id) {
                PropertyContainer props = productData.get(idFactory.parse(id));
                if (props == null)
                    return null;
                return props.asMap();
            }
        };
        RecommendationModel model =
            new RecommendationModelImpl<ID<String>>(
                                                    graph, productRepo, idFactory);
        RecommendationService service = new RecommendationService(model);
        return service;
    }

    public static void main(String[] args) throws IOException {
        int i = 0;
        String graph = args[i++];
        String productDataFile = args[i++];
        String productFormatFile = args[i++];

        long t0 = System.currentTimeMillis();
        RecommendationService service = setup(graph, productDataFile,
                                              productFormatFile);
        long t1 = System.currentTimeMillis();
        System.out.println(service);
        System.out.println("Done in " + (t1 - t0) + " ms.");
    }

    @Override
    public String toString() {
        return "RecommendationService [model=" + model + "]";
    }
}
