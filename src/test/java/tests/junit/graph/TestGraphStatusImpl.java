package tests.junit.graph;

import org.junit.Test;

import junit.framework.Assert;
import recng.graph.GraphStats;
import recng.graph.GraphStatsImpl;

public class TestGraphStatusImpl {

    @Test
    public void testNumberOfTraversals() {
        GraphStats status = new GraphStatsImpl();
        Assert.assertEquals("No traversals should have been registered",
                            0, status.getTraversals());
        status.incTraversals();
        Assert.assertEquals("A traversal should have been registered",
                            1, status.getTraversals());
        status.incTraversals();
        Assert.assertEquals("Another traversal should have been registered",
                            2, status.getTraversals());
    }

    @Test
    public void testTraversedEdges() {
        GraphStats status = new GraphStatsImpl();
        Assert.assertEquals("Expected no traversed edges",
                            0, status.getTraversedEdges());
        status.incTraversedEdges(1);
        Assert.assertEquals("Expected 1 traversed edge",
                            1, status.getTraversedEdges());
        status.incTraversedEdges(2);
        Assert.assertEquals("Expected 3 traversed edge",
                            3, status.getTraversedEdges());
    }

    @Test
    public void testToString() {
        GraphStats status = new GraphStatsImpl();
        String s = status.toString();
        Assert.assertNotNull(s);
    }
}
