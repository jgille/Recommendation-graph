package recng.graph;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base graph status implementation.
 *
 * @author jon
 *
 */
public class GraphStatsImpl implements GraphStats {

    private final Date initTime;
    private final AtomicInteger traversals;
    private final AtomicInteger traversedEdges;
    private final AtomicInteger maxTraversedEdges;

    public GraphStatsImpl() {
        this.initTime = new Date();
        this.traversals = new AtomicInteger();
        this.maxTraversedEdges = new AtomicInteger();
        this.traversedEdges = new AtomicInteger();
    }

    @Override
    public Date getInitTime() {
        return initTime;
    }

    @Override
    public int getTraversals() {
        return traversals.get();
    }

    @Override
    public void incTraversals() {
        traversals.incrementAndGet();
    }

    @Override
    public int getTraversedEdges() {
        return traversedEdges.get();
    }

    @Override
    public int getMaxTraversedEdges() {
        return maxTraversedEdges.get();
    }

    @Override
    public void incTraversedEdges(int delta) {
        traversedEdges.addAndGet(delta);
        if (maxTraversedEdges.get() < delta) {
            // Not totally synchronized here, but that shouln't matter much
            maxTraversedEdges.set(delta);
        }
    }

    @Override
    public String toString() {
        int nofTraversals = getTraversals();
        int travEdges = getTraversedEdges();
        double avgTraversedEdges =
            nofTraversals > 0 ? 1d * travEdges / nofTraversals : 0;
        return String.format(
                             "Init time: %s\n" +
                                 "Traversals: %d\n" +
                                 "Tot traversed edges: %d\n" +
                                 "Max traversed edges for a traversal: %d\n" +
                                 "Avg traversed edges per traversal: %.1f",
                             initTime, nofTraversals,
                             travEdges, maxTraversedEdges.get(), avgTraversedEdges);
    }
}
