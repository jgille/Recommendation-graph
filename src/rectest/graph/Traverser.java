package rectest.graph;

import java.util.List;

/**
 * A traverser used to traverse edges between product nodes in a graph.
 *
 * @author Jon Ivmark
 */
public interface Traverser<K> {

    /**
     * Gets and iterator used to traverse product nodes connected to a product,
     * according to the rules setup in the traverser.
     *
     * Neighbors are traversed ordered by descending edge weight.
     *
     * NOTE: Make sure you always close the cursor once you've started to
     * traverse it, i.e. close it in a finally block.
     */
    GraphCursor<K> traverse();

    /**
     * Gets the entire traversal path according to the rules setup in the
     * traverser.
     *
     * Neighbors are traversed ordered by descending edge weight.
     */
    List<GraphEdge<K>> getPath();
}
