package recng.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import recng.cache.Cache;
import recng.cache.CacheBuilder;

/**
 * Imports graphs from csv files.
 *
 * @author jon
 */
public abstract class GraphImporterImpl<T> implements GraphImporter<T> {

    private final GraphBuilder<T> builder;
    private final Map<String, EdgeType> edgeTypes =
        new HashMap<String, EdgeType>();

    /**
     * Avoid using different instances for equivalent keys
     */
    private final Cache<String, NodeId<T>> keyCache =
        new CacheBuilder<String, NodeId<T>>()
        .concurrencyLevel(1).maxSize(50000).build();

    public GraphImporterImpl(GraphBuilder<T> builder,
                             Collection<EdgeType> edgeTypes) {
        this.builder = builder;
        for (EdgeType edgeType : edgeTypes)
            this.edgeTypes.put(edgeType.name(), edgeType);
    }

    public Graph<T> importGraph(String file) {
        try {
            return importCSV(file);
        } catch (IOException e) {
            throw new RuntimeException("Import failed", e);
        }
    }

    private Graph<T> importCSV(String file) throws IOException {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line = null;
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";");
                if (fields.length < 3 || fields.length > 4)
                    throw new IllegalArgumentException(
                                                       "Invalid number of fields ("
                                                           + fields.length
                                                           + ") for line: "
                                                           + line);
                int i = 0;
                String from = fields[i++];
                String to = fields[i++];
                EdgeType edgeType = edgeTypes.get(fields[i++]);
                if (edgeType == null)
                    throw new IllegalArgumentException(
                                                       "Illegal edge type for line: "
                                                           + line);
                float weight =
                    fields.length > i ? Float.parseFloat(fields[i++]) : -1f;
                builder.addEdge(getKey(from), getKey(to), edgeType,
                                weight);
                if (count % 10000 == 0)
                    System.out.println("Imported " + count + " edges");
                count++;
            }
        } finally {
            if (br != null)
                br.close();
            if (fr != null)
                fr.close();
        }
        return builder.build();
    }

    private NodeId<T> getKey(String id) {
        if (keyCache.contains(id))
            return keyCache.get(id);
        NodeId<T> key = getNodeKey(id);
        keyCache.cache(id, key);
        return key;
    }

    /**
     * Creates a node key from a string.
     * 
     * TODO: This needs work, how do we distinguish between different node
     * types? Regex?
     */
    protected abstract NodeId<T> getNodeKey(String id);
}
