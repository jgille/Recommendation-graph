package tests.junit.graph;

import org.junit.Test;

import junit.framework.Assert;
import recng.graph.GraphStatus;
import recng.graph.GraphStatusImpl;

public class TestGraphStatusImpl {

    @Test
    public void testNumberOfTraversals() {
        GraphStatus status = new GraphStatusImpl();
        Assert.assertEquals("No traversals should have been registered",
                            0, status.getNumberOfTraversals());
        status.incNumberOfTraversals();
        Assert.assertEquals("A traversal should have been registered",
                            1, status.getNumberOfTraversals());
        status.incNumberOfTraversals();
        Assert.assertEquals("Another traversal should have been registered",
                            2, status.getNumberOfTraversals());
    }

    @Test
    public void testRequestedEdges() {
        GraphStatus status = new GraphStatusImpl();
        Assert.assertEquals("Expected no requested edges",
                            0, status.getRequestedEdges());
        status.incRequestedEdges(1);
        Assert.assertEquals("Expected 1 requested edge",
                            1, status.getRequestedEdges());
        status.incRequestedEdges(3);
        Assert.assertEquals("Expected 4 requested edge",
                            4, status.getRequestedEdges());
    }

    @Test
    public void testReturnedEdges() {
        GraphStatus status = new GraphStatusImpl();
        Assert.assertEquals("Expected no returned edges",
                            0, status.getReturnedEdges());
        status.incReturnedEdges(1);
        Assert.assertEquals("Expected 1 returned edge",
                            1, status.getReturnedEdges());
        status.incReturnedEdges(2);
        Assert.assertEquals("Expected 3 returned edge",
                            3, status.getReturnedEdges());
    }

    @Test
    public void testTraversedEdges() {
        GraphStatus status = new GraphStatusImpl();
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
    public void testTraversalTime() {
        GraphStatus status = new GraphStatusImpl();
        Assert.assertEquals("Expected no traversals yet",
                            0, status.getTraversalTime());
        status.incTraversalTime(5);
        Assert.assertEquals("Total traversal time should be set to 5",
                            5, status.getTraversalTime());
        Assert.assertEquals("Max traversal time should be set to 5",
                            5, status.getMaxTraversalTime());
        status.incTraversalTime(15);
        Assert
            .assertEquals("Total traversal time should have been incremented by 15",
                          20, status.getTraversalTime());
        Assert.assertEquals("Max traversal time should be set to 15",
                            15, status.getMaxTraversalTime());
    }

    @Test
    public void testToString() {
        GraphStatus status = new GraphStatusImpl();
        String s = status.toString();
        Assert.assertNotNull(s);
    }
}
