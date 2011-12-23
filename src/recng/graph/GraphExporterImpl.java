package recng.graph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import recng.common.Consumer;

/**
 * Exports graphs to file in csv format.
 *
 * @author jon
 */
public abstract class GraphExporterImpl<T> implements GraphExporter<T> {

    public void exportGraph(Graph<T> graph, String file) {
        try {
            FileWriter fw = null;
            BufferedWriter bw = null;
            PrintWriter pw = null;
            try {
                fw = new FileWriter(file);
                bw = new BufferedWriter(fw);
                pw = new PrintWriter(fw);
                Exporter<T> exporter = new Exporter<T>(pw) {
                    @Override
                    protected String serialize(NodeId<T> node) {
                        return GraphExporterImpl.this.serialize(node);
                    }
                };
                graph.getEdges(exporter);
            } finally {
                if (pw != null)
                    pw.close();
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static abstract class Exporter<K> implements
        Consumer<GraphEdge<K>, Void> {

        private final PrintWriter pw;

        public Exporter(PrintWriter pw) {
            this.pw = pw;
        }

        @Override
        public Void
            consume(GraphEdge<K> edge) {
            String line =
                String.format("%s;%s;%s;%s", serialize(edge.getStartNode()),
                              serialize(edge.getEndNode()), edge.getType()
                                  .name(),
                              edge.getWeight());
            pw.println(line);
            return null;
        }

        protected abstract String serialize(NodeId<K> node);
    };

    /**
     * Serialized a node to a string.
     */
    protected abstract String serialize(NodeId<T> node);
}
