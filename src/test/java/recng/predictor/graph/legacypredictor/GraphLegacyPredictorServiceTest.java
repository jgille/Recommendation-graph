package recng.predictor.graph.legacypredictor;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import recng.graph.Graph;
import recng.graph.GraphEdge;
import recng.graph.NodeID;
import recng.graph.Traverser;
import recng.index.ID;
import recng.index.StringIDs;
import recng.predictor.graph.GraphPredictorBaseData;
import recng.recommendations.domain.RecommendationNodeType;
import recng.recommendations.graph.RecommendationEdgeType;

public class GraphLegacyPredictorServiceTest {

    /**
     * Simplest possible test case, two users each buying the same two products.
     */
    @Test
    public void simpleTest() {
        GraphPredictorBaseData baseData =
            new GraphPredictorBaseData();
        baseData.addPurchasedProduct("u1", "p1");
        baseData.addPurchasedProduct("u1", "p2");
        baseData.addPurchasedProduct("u2", "p1");
        baseData.addPurchasedProduct("u2", "p2");

        GraphLegacyPredictorService service =
            new GraphLegacyPredictorService();
        Graph<ID<String>> graph = service.createPredictions(baseData);
        // test p1 as input
        Traverser<ID<String>> traverser =
            graph.getTraverser(new NodeID<ID<String>>(StringIDs.parseID("p1"),
                                                      RecommendationNodeType.PRODUCT),
                               RecommendationEdgeType.PEOPLE_WHO_BOUGHT);
        MatcherAssert.assertThat(traverser, Matchers.notNullValue());
        List<GraphEdge<ID<String>>> edges = traverser.getPath();
        MatcherAssert.assertThat(edges.size(), Matchers.equalTo(1));
        MatcherAssert.assertThat(edges.get(0).getEndNode().getID().getID(),
                                 Matchers.is("p2"));
        double weight = edges.get(0).getWeight();
        MatcherAssert.assertThat(weight, Matchers.closeTo(2.0 / 3.0, 0.1));

        // test p2 as input
        traverser =
            graph.getTraverser(new NodeID<ID<String>>(StringIDs.parseID("p1"),
                                                      RecommendationNodeType.PRODUCT),
                               RecommendationEdgeType.PEOPLE_WHO_BOUGHT);
        MatcherAssert.assertThat(traverser, Matchers.notNullValue());
        edges = traverser.getPath();
        MatcherAssert.assertThat(edges.size(), Matchers.equalTo(1));
        MatcherAssert.assertThat(edges.get(0).getEndNode().getID().getID(),
                                 Matchers.is("p2"));
        weight = edges.get(0).getWeight();
        MatcherAssert.assertThat(weight, Matchers.closeTo(2.0 / 3.0, 0.1));
    }

    /**
     * Tests that top sellers are punished with legacy predictor. There are as
     * many connections between p1 and p2 as there is between p1 and p3, but p3
     * is a "big seller" and is thus penalized.
     */
    @Test
    public void testPunishTopSellers() {
        // create data
        GraphPredictorBaseData baseData =
            new GraphPredictorBaseData();
        baseData.addPurchasedProduct("u1", "p1");
        baseData.addPurchasedProduct("u1", "p2");
        baseData.addPurchasedProduct("u2", "p1");
        baseData.addPurchasedProduct("u2", "p2");
        baseData.addPurchasedProduct("u3", "p1");
        baseData.addPurchasedProduct("u3", "p3");
        baseData.addPurchasedProduct("u4", "p1");
        baseData.addPurchasedProduct("u4", "p3");
        baseData.addPurchasedProduct("u5", "p3");
        baseData.addPurchasedProduct("u5", "p4");

        // Create graph
        GraphLegacyPredictorService service =
            new GraphLegacyPredictorService();
        Graph<ID<String>> graph = service.createPredictions(baseData);

        // Traverse graph from p1 as input
        Traverser<ID<String>> traverser =
            graph.getTraverser(new NodeID<ID<String>>(StringIDs.parseID("p1"),
                                                      RecommendationNodeType.PRODUCT),
                               RecommendationEdgeType.PEOPLE_WHO_BOUGHT);

        MatcherAssert.assertThat(traverser, Matchers.notNullValue());

        // Slot 0
        List<GraphEdge<ID<String>>> edges = traverser.getPath();
        MatcherAssert.assertThat(edges.size(), Matchers.equalTo(2));
        MatcherAssert.assertThat(edges.get(0).getEndNode().getID().getID(),
                                 Matchers.is("p2"));
        double weight = edges.get(0).getWeight();
        MatcherAssert.assertThat(weight, Matchers.closeTo(2.0 / 3.0, 0.01));

        // Slot 1
        MatcherAssert.assertThat(edges.get(1).getEndNode().getID().getID(),
                                 Matchers.is("p3"));
        weight = edges.get(1).getWeight();
        MatcherAssert.assertThat(weight, Matchers.closeTo(2.0 / 4.0, 0.01));

    }

}
