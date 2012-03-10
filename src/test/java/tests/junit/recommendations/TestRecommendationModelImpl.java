package tests.junit.recommendations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;

import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.TableMetadata;
import recng.common.TableMetadataImpl;
import recng.db.InMemoryKVStore;
import recng.db.KVStore;
import recng.graph.Graph;
import recng.graph.ImmutableGraphImpl;
import recng.graph.GraphMetadata;
import recng.graph.NodeID;
import recng.index.ID;
import recng.recommendations.IDParser;
import recng.recommendations.ProductQuery;
import recng.recommendations.ProductQueryImpl;
import recng.recommendations.RecommendationModel;
import recng.recommendations.RecommendationModelImpl;
import recng.recommendations.StringIDParser;
import recng.recommendations.data.ProductRepository;
import recng.recommendations.data.ProductRepositoryImpl;
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.domain.Product;
import recng.recommendations.domain.RecommendationNodeType;
import recng.recommendations.graph.RecommendationGraphMetadata;
import recng.recommendations.graph.RecommendationEdgeType;

/**
 * Tests {@link RecommendationModelImpl}.
 *
 * @author jon
 *
 */
public class TestRecommendationModelImpl {

    private static final RecommendationEdgeType EDGE_TYPE =
        RecommendationEdgeType.PEOPLE_WHO_BOUGHT;

    private static final GraphMetadata GRAPH_METADATA =
        RecommendationGraphMetadata.getInstance();

    private static final IDParser<ID<String>> FAC = new StringIDParser();

    private static final NodeID<ID<String>> N1 =
        new NodeID<ID<String>>(FAC.parse("1"),
                               RecommendationNodeType.PRODUCT);

    private static final NodeID<ID<String>> N2 =
        new NodeID<ID<String>>(FAC.parse("2"),
                               RecommendationNodeType.PRODUCT);
    private static final NodeID<ID<String>> N3 =
        new NodeID<ID<String>>(FAC.parse("3"),
                               RecommendationNodeType.PRODUCT);
    private static final NodeID<ID<String>> N4 =
        new NodeID<ID<String>>(FAC.parse("4"),
                               RecommendationNodeType.PRODUCT);

    private static final Float N1_N2 = 0.7f;
    private static final Float N1_N3 = 0.3f;
    private static final Float N2_N4 = 0.6f;
    private static final Float N2_N1 = 0.4f;
    private static final Float N3_N1 = 1.0f;
    private static final Float N4_N2 = 1.0f;

    /*
     * 1 -- w=0.7 --> 2;
     * 1 -- w=0.3 --> 3;
     * 2 -- w=0.6 --> 4;
     * 2 -- w=0.4 --> 1;
     * 3 -- w=1.0 --> 1;
     * 4 -- w=1.0 --> 2;
     */
    private Graph<ID<String>> buildGraph() {
        ImmutableGraphImpl.Builder<ID<String>> builder =
            ImmutableGraphImpl.Builder.create(GRAPH_METADATA);

        int k1 = builder.addOrGetNode(N1);
        int k2 = builder.addOrGetNode(N2);
        int k3 = builder.addOrGetNode(N3);
        int k4 = builder.addOrGetNode(N4);

        builder.addEdge(k1, k3, EDGE_TYPE, N1_N3);
        builder.addEdge(k1, k2, EDGE_TYPE, N1_N2);
        builder.addEdge(k2, k1, EDGE_TYPE, N2_N1);
        builder.addEdge(k2, k4, EDGE_TYPE, N2_N4);
        builder.addEdge(k3, k1, EDGE_TYPE, N3_N1);
        builder.addEdge(k4, k2, EDGE_TYPE, N4_N2);

        return builder.build();
    }

    private ProductRepository createProductRepo() {
        KVStore<String, Map<String, Object>> db =
            new InMemoryKVStore<String, Map<String, Object>>();
        Map<String, Object> p1 = new HashMap<String, Object>();
        p1.put(Product.ID_PROPERTY, N1.getID().getID());
        p1.put("name", N1.getID().getID());
        p1.put("index", 1);
        p1.put("__is_valid", true);
        p1.put("__categories", Arrays.<String> asList("cat1", "cat2"));
        db.put(N1.getID().getID(), p1);
        Map<String, Object> p2 = new HashMap<String, Object>();
        p2.put(Product.ID_PROPERTY, N2.getID().getID());
        p2.put("name", N2.getID().getID());
        p2.put("index", 2);
        p2.put("__categories", Arrays.<String> asList("cat2", "cat3"));
        p2.put("__is_valid", true);
        db.put(N2.getID().getID(), p2);
        Map<String, Object> p3 = new HashMap<String, Object>();
        p3.put(Product.ID_PROPERTY, N3.getID().getID());
        p3.put("name", N3.getID().getID());
        p3.put("index", 3);
        p3.put("__categories", Arrays.<String> asList("cat3", "cat4"));
        p3.put("__is_valid", true);
        db.put(N3.getID().getID(), p3);
        Map<String, Object> p4 = new HashMap<String, Object>();
        p4.put(Product.ID_PROPERTY, N4.getID().getID());
        p4.put("name", N4.getID().getID());
        p4.put("index", 4);
        p4.put("__categories", Arrays.<String> asList("cat3"));
        p4.put("__is_valid", true);
        db.put(N4.getID().getID(), p4);

        FieldMetadata id =
            FieldMetadataImpl.create(Product.ID_PROPERTY, FieldMetadata.Type.STRING);
        FieldMetadata name =
            FieldMetadataImpl.create("name", FieldMetadata.Type.STRING);
        FieldMetadata index =
            FieldMetadataImpl.create("index", FieldMetadata.Type.INT);
        FieldMetadata categories =
            new FieldMetadataImpl.Builder("__categories",
                                          FieldMetadata.Type.STRING)
                .setRepeated(true).build();

        FieldMetadata isValid =
            FieldMetadataImpl.create("__is_valid", FieldMetadata.Type.BOOLEAN);
        TableMetadata fs =
            new TableMetadataImpl(Arrays.asList(id, name, index, categories,
                                                isValid));
        return new ProductRepositoryImpl(db, fs);
    }

    private RecommendationModel getModel() {
        Graph<ID<String>> productGraph = buildGraph();
        ProductRepository productMetadata = createProductRepo();
        return new RecommendationModelImpl<ID<String>>(productGraph,
                                                       productMetadata,
                                                       FAC);
    }

    @Test
    public void testGetRelatedProducts() {
        ProductQuery query = getDefaultQuery();
        RecommendationModel model = getModel();
        List<ImmutableProduct> related =
            model.getRelatedProducts(N1.getID().getID(), query);

        assertNotNull(related);
        assertEquals(2, related.size());
        ImmutableProduct r1 = related.get(0);
        assertEquals(N2.getID().getID(), r1.getID());
        assertEquals(2, r1.getProperty("index"));
        assertEquals("2", r1.getProperty("name"));
        assertEquals(Arrays.<String> asList("cat2", "cat3"), r1.getCategories());

        ImmutableProduct r2 = related.get(1);

        assertEquals(N3.getID().getID(), r2.getID());
        assertEquals(3, r2.getProperty("index"));
        assertEquals("3", r2.getProperty("name"));
        assertEquals(Arrays.<String> asList("cat3", "cat4"), r2.getCategories());
    }

    @Test
    public void testMultipleSourceProducts() {
        ProductQuery query = getDefaultQuery();
        RecommendationModel model = getModel();
        List<ImmutableProduct> related =
            model.getRelatedProducts(Arrays.asList(N2.getID().getID(), N3.getID().getID()), query);
        assertNotNull(related);
        assertEquals(2, related.size());
        ImmutableProduct r1 = related.get(0);
        assertEquals(N1.getID().getID(), r1.getID());
        assertEquals(1, r1.getProperty("index"));
        assertEquals("1", r1.getProperty("name"));
        assertEquals(Arrays.<String> asList("cat1", "cat2"), r1.getCategories());

        ImmutableProduct r2 = related.get(1);

        assertEquals(N4.getID().getID(), r2.getID());
        assertEquals(4, r2.getProperty("index"));
        assertEquals("4", r2.getProperty("name"));
        assertEquals(Arrays.<String> asList("cat3"), r2.getCategories());

    }

    private ProductQuery getDefaultQuery() {
        return new ProductQueryImpl(2, EDGE_TYPE);
    }
}
