package tests.junit.predictor.graph;

import gnu.trove.TIntCollection;

import org.junit.Test;
import static org.junit.Assert.*;

import recng.predictor.graph.GraphPredictorBaseData;
import recng.recommendations.domain.RecommendationNodeType;

/**
 * Basic test for {@link GraphPredictorBaseData}.
 *
 * @author jon
 *
 */
public class TestGraphPredictorBaseData {

    @Test
    public void testSimple() {
        GraphPredictorBaseData baseData = new GraphPredictorBaseData(true);
        baseData.addPurchasedProduct("u1", "p1");
        baseData.addPurchasedProduct("u1", "p2");
        baseData.addPurchasedProduct("u2", "p1");
        baseData.addPurchasedProduct("u2", "p3");
        baseData.addViewedProduct("s1", "p1");
        baseData.addViewedProduct("s2", "p2");
        baseData.addViewedProduct("s2", "p3");

        int u1 = baseData.getIndex(RecommendationNodeType.USER, "u1");
        int u2 = baseData.getIndex(RecommendationNodeType.USER, "u2");

        int s1 = baseData.getIndex(RecommendationNodeType.SESSION, "s1");
        int s2 = baseData.getIndex(RecommendationNodeType.SESSION, "s2");

        int p1 = baseData.getIndex(RecommendationNodeType.PRODUCT, "p1");
        int p2 = baseData.getIndex(RecommendationNodeType.PRODUCT, "p2");
        int p3 = baseData.getIndex(RecommendationNodeType.PRODUCT, "p3");

        TIntCollection u1p = baseData.getPurchasedProducts(u1);
        assertEquals(2, u1p.size());
        assertTrue(u1p.containsAll(new int[] { p1, p2 }));

        TIntCollection u2p = baseData.getPurchasedProducts(u2);
        assertEquals(2, u1p.size());
        assertTrue(u2p.containsAll(new int[] { p1, p3 }));

        TIntCollection s1p = baseData.getViewedProducts(s1);
        assertEquals(1, s1p.size());
        assertTrue(s1p.containsAll(new int[] { p1 }));

        TIntCollection s2p = baseData.getViewedProducts(s2);
        assertEquals(2, s2p.size());
        assertTrue(s2p.containsAll(new int[] { p2, p3 }));
    }
}
