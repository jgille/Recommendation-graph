package recng.graph;

/**
 * Classes used to export a graph to file should implement this interface.
 * 
 * @author jon
 */
public interface GraphExporter<T> {

    void exportGraph(Graph<T> graph, String file);

}
