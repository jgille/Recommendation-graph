package recng.graph;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base graph status implementation.
 *
 * @author jon
 *
 */
public class GraphStatusImpl implements GraphStatus {

    private final Date initTime;
    private final AtomicInteger traversals;
    private final AtomicLong requestedEdges;
    private final AtomicInteger returnedEdges;
    private final AtomicInteger traversedEdges;
    private final AtomicInteger maxTraversedEdges;
    private final AtomicLong traversalTime;
    private final AtomicLong maxTraversalTime;

    public GraphStatusImpl() {
        this.initTime = new Date();
        this.traversals = new AtomicInteger();
        this.requestedEdges = new AtomicLong();
        this.returnedEdges = new AtomicInteger();
        this.maxTraversedEdges = new AtomicInteger();
        this.traversedEdges = new AtomicInteger();
        this.traversalTime = new AtomicLong();
        this.maxTraversalTime = new AtomicLong();
    }

    @Override
    public Date getInitTime() {
        return initTime;
    }

    @Override
    public int getNumberOfTraversals() {
        return traversals.get();
    }

    @Override
    public void incNumberOfTraversals() {
        traversals.incrementAndGet();
    }

    @Override
    public long getRequestedEdges() {
        return requestedEdges.get();
    }

    @Override
    public void incRequestedEdges(int delta) {
        requestedEdges.addAndGet(delta);
    }

    @Override
    public int getReturnedEdges() {
        return returnedEdges.get();
    }

    @Override
    public void incReturnedEdges(int delta) {
        returnedEdges.addAndGet(delta);
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
    public long getTraversalTime() {
        return traversalTime.get();
    }

    @Override
    public void incTraversalTime(long delta) {
        traversalTime.addAndGet(delta);
        if (maxTraversalTime.get() < delta) {
            // Not totally synchronized here, but that shouln't matter much
            maxTraversalTime.set(delta);
        }
    }

    @Override
    public long getMaxTraversalTime() {
        return maxTraversalTime.get();
    }

    @Override
    public String toString() {
        long now = System.currentTimeMillis();
        int nofTraversals = getNumberOfTraversals();
        long dt = Math.max(now - initTime.getTime(), 1);
        int traversalsPerSec =
            (int) Math.round(1000d * nofTraversals / dt);
        long reqEdges = getRequestedEdges();
        double avgReqEdges =
            nofTraversals > 0 ? 1d * reqEdges / nofTraversals : 0;
        int retEdges = getReturnedEdges();
        double avgRetEdges =
            nofTraversals > 0 ? 1d * retEdges / nofTraversals : 0;
        int travEdges = getTraversedEdges();
        double avgTraversedEdges =
            nofTraversals > 0 ? 1d * travEdges / nofTraversals : 0;
        int maxTravEdges = getMaxTraversedEdges();
        double filterPercentage =
            travEdges > 0 ? 100 - 100d * retEdges / travEdges : 0;
        long totTravTime = getTraversalTime();
        long maxTravTime = getMaxTraversalTime();
        int avgTravTime =
            totTravTime > 0 ? (int) Math
                .round(1d * totTravTime / nofTraversals) : 0;

        return String.format(
                             "Init time: %s\n" +
                                 "Traversals: %d\n" +
                                 "Avg traversals/sec: %d\n" +
                                 "Requested edges: %d\n" +
                                 "Avg requested edges per traversal: %.1f\n" +
                                 "Returned edges: %d\n" +
                                 "Avg returned edges per traversal: %.1f\n" +
                                 "Traversed edges: %d\n" +
                                 "Avg traversed edges per traversal: %.1f\n" +
                                 "Max traversed edges per traversal: %d\n" +
                                 "Filtered: %.1f %%\n" +
                                 "Avg traversal time (ms): %d\n" +
                                 "Max traversal time (ms): %d\n",
                             initTime, nofTraversals, traversalsPerSec,
                             reqEdges, avgReqEdges, retEdges, avgRetEdges,
                             travEdges, avgTraversedEdges, maxTravEdges,
                             filterPercentage, avgTravTime,
                             maxTravTime);
    }
}
