package tests.junit.recommendations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.TableMetadata;
import recng.common.TableMetadataImpl;
import recng.common.Marshallers;
import recng.db.InMemoryKVStore;
import recng.db.KVStore;
import recng.graph.Graph;
import recng.graph.GraphImpl;
import recng.graph.NodeID;
import recng.index.ID;
import recng.index.StringIDs;
import recng.recommendations.IDFactory;
import recng.recommendations.ImmutableProduct;
import recng.recommendations.ProductID;
import recng.recommendations.ProductDataStore;
import recng.recommendations.ProductDataStoreImpl;
import recng.recommendations.ProductQuery;
import recng.recommendations.RecommendationModel;
import recng.recommendations.RecommendationModelImpl;
import recng.recommendations.RecommendationType;
import recng.recommendations.filter.ProductFilter;

public class TestRecommendationModelImpl {

    private static final RecommendationType EDGE_TYPE =
        RecommendationType.PEOPLE_WHO_BOUGHT;

    private static final IDFactory<ID<String>> KP =
        new IDFactory<ID<String>>() {
            public ID<String> fromString(String id) {
                return StringIDs.parseKey(id);
            }

            public String toString(ID<String> productId) {
                return productId.getID();
            }
        };

    private static final NodeID<ID<String>> N1 =
        new ProductID<ID<String>>(KP.fromString("1"));
    private static final NodeID<ID<String>> N2 =
        new ProductID<ID<String>>(KP.fromString("2"));
    private static final NodeID<ID<String>> N3 =
        new ProductID<ID<String>>(KP.fromString("3"));
    private static final NodeID<ID<String>> N4 =
        new ProductID<ID<String>>(KP.fromString("4"));

    private static final Float N1_N2 = 0.7f;
    private static final Float N1_N3 = 0.3f;
    private static final Float N2_N4 = 0.6f;
    private static final Float N2_N1 = 0.4f;
    private static final Float N3_N1 = 1.0f;
    private static final Float N4_N2 = 1.0f;

    /**
     * 1 -- w=0.7 --> 2; 1 -- w=0.3 --> 3; 2 -- w=0.6 --> 4; 2 -- w=0.4 --> 1; 3
     * -- w=1.0 --> 1; 4 -- w=1.0 --> 2;
     */
    private Graph<ID<String>> buildGraph() {
        GraphImpl.Builder<ID<String>> builder =
            new GraphImpl.Builder<ID<String>>();

        builder.addEdge(N1, N3, EDGE_TYPE, N1_N3)
            .addEdge(N1, N2, EDGE_TYPE, N1_N2)
            .addEdge(N2, N1, EDGE_TYPE, N2_N1)
            .addEdge(N2, N4, EDGE_TYPE, N2_N4)
            .addEdge(N3, N1, EDGE_TYPE, N3_N1)
            .addEdge(N4, N2, EDGE_TYPE, N4_N2);

        return builder.build();
    }

    private ProductDataStore createMetadata() {
        KVStore<String, Map<String, Object>> db =
            new InMemoryKVStore<String, Map<String, Object>>();
        Map<String, Object> p1 = new HashMap<String, Object>();
        p1.put("name", N1.getID().getID());
        p1.put("index", 1);
        p1.put("__is_valid", true);
        p1.put("__categories", Arrays.<String> asList("cat1", "cat2"));
        db.put(N1.getID().getID(), p1);
        Map<String, Object> p2 = new HashMap<String, Object>();
        p2.put("name", N2.getID().getID());
        p2.put("index", 2);
        p2.put("__categories", Arrays.<String> asList("cat2", "cat3"));
        p2.put("__is_valid", true);
        db.put(N2.getID().getID(), p2);
        Map<String, Object> p3 = new HashMap<String, Object>();
        p3.put("name", N3.getID().getID());
        p3.put("index", 3);
        p3.put("__categories", Arrays.<String> asList("cat3", "cat4"));
        p3.put("__is_valid", true);
        db.put(N3.getID().getID(), p3);
        Map<String, Object> p4 = new HashMap<String, Object>();
        p4.put("name", N4.getID().getID());
        p4.put("index", 4);
        p4.put("__categories", Arrays.<String> asList("cat3"));
        p4.put("__is_valid", true);
        db.put(N4.getID().getID(), p4);

        FieldMetadata<String> name =
            new FieldMetadataImpl<String>("name",
                                          Marshallers.STRING_MARSHALLER,
                                          FieldMetadata.Type.STRING);
        FieldMetadata<Integer> index =
            new FieldMetadataImpl<Integer>("index",
                                           Marshallers.INTEGER_MARSHALLER,
                                           FieldMetadata.Type.INTEGER);
        FieldMetadata<String> categories =
            new FieldMetadataImpl<String>("__categories",
                                          Marshallers.STRING_MARSHALLER,
                                          FieldMetadata.Type.STRING, true);
        FieldMetadata<Boolean> isValid =
            new FieldMetadataImpl<Boolean>("__is_valid",
                                           Marshallers.BOOLEAN_MARSHALLER,
                                           FieldMetadata.Type.BOOLEAN);
        TableMetadata fs =
            new TableMetadataImpl.Builder().add(name).add(index).add(categories)
                .add(isValid).build();
        return new ProductDataStoreImpl(db, fs);
    }

    private RecommendationModel getModel() {
        Graph<ID<String>> productGraph = buildGraph();
        ProductDataStore productMetadata = createMetadata();
        return new RecommendationModelImpl<ID<String>>(productGraph,
                                                        productMetadata,
                                                        KP);
    }

    @Test
    public void testGetRelatedProducts() {
        ProductQuery query = new ProductQuery() {
            public int getLimit() {
                return 2;
            }

            public ProductFilter getFilter() {
                return new ProductFilter() {
                    public boolean accepts(ImmutableProduct product) {
                        Integer index = product.getProperty("index");
                        return index.intValue() > 2;
                    }

                    public Set<String> getFilterProperties() {
                        return Collections.<String> emptySet();
                    }
                };
            }

            public int getMaxCursorSize() {
                return 1000;
            }

            public int getMaxRelationDistance() {
                return 2;
            }

            public RecommendationType getRecommendationType() {
                return EDGE_TYPE;
            }

        };
        RecommendationModel model = getModel();
        Set<String> properties = new HashSet<String>();
        properties.add("name");
        properties.add("index");
        List<ImmutableProduct> related =
            model.getRelatedProducts(N1.getID().getID(), query, properties);

        assertNotNull(related);
        assertEquals(2, related.size());
        ImmutableProduct r1 = related.get(0);
        assertEquals(N3.getID().getID(), r1.getId());
        assertEquals(3, r1.getProperty("index"));
        assertEquals("3", r1.getProperty("name"));
        assertEquals(Arrays.<String> asList("cat3", "cat4"), r1.getCategories());

        ImmutableProduct r2 = related.get(1);

        assertEquals(N4.getID().getID(), r2.getId());
        assertEquals(4, r2.getProperty("index"));
        assertEquals("4", r2.getProperty("name"));
        assertEquals(Arrays.<String> asList("cat3"), r2.getCategories());
    }
}
