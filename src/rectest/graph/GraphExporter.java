package rectest.graph;

/**
 * Classes used to export a graph to file should implement this interface.
 * 
 * @author jon
 */
public interface GraphExporter<K> {

    void exportGraph(Graph<K> graph, String file);

}
