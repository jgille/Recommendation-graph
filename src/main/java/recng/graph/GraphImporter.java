package recng.graph;

/**
 * Classes used to import a graph from file should implement this interface.
 * 
 * @author jon
 */
public interface GraphImporter<T> {

    Graph<T> importGraph(String file);

}
