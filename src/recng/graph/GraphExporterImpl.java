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
                pw.println("# Graph metadata");
                pw.println("#");
                pw.println("# Node types (name: ordinal)");
                for (NodeType nodeType : graph.getMetadata().getNodeTypes()) {
                    pw.println(String.format("# %s: %s", nodeType.name(),
                                             nodeType.ordinal()));
                }
                pw.println("#");
                pw.println("# Edge types (name: ordinal)");
                for (EdgeType edgeType : graph.getMetadata().getEdgeTypes()) {
                    pw.println(String.format("# %s: %s", edgeType.name(),
                                             edgeType.ordinal()));
                }
                pw.println("#");
                pw.println("# Nodes (index;id;node type ordinal)");
                NodeExporter nodeExporter = new NodeExporter(graph, pw);
                graph.getAllNodes(nodeExporter);
                pw.println("# Edges (start node index;end node index;" +
                    "edge type ordinal;edge weight)");
                EdgeExporter<T> edgeExporter = new EdgeExporter<T>(graph, pw);
                graph.getAllEdges(edgeExporter);
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

    private class NodeExporter implements
        Consumer<NodeID<T>, Void> {

        private final Graph<T> graph;
        private final PrintWriter pw;
        private int count = 0;

        public NodeExporter(Graph<T> graph, PrintWriter pw) {
            this.graph = graph;
            this.pw = pw;
        }

        @Override
        public Void consume(NodeID<T> node) {
            String line =
                String.format("%s;%s;%s",
                              graph.getPrimaryKey(node),
                              GraphExporterImpl.this.serializeNode(node),
                              node.getNodeType().ordinal());
            pw.println(line);
            count++;
            if (count % 100000 == 0)
                System.out.println("Exported " + count + " nodes..");

            return null;
        }
    }

    private static class EdgeExporter<T> implements
        Consumer<GraphEdge<T>, Void> {

        private final Graph<T> graph;
        private final PrintWriter pw;
        private int count = 0;

        public EdgeExporter(Graph<T> graph, PrintWriter pw) {
            this.graph = graph;
            this.pw = pw;
        }

        @Override
        public Void
            consume(GraphEdge<T> edge) {
            String line =
                String.format("%s;%s;%s;%s",
                              graph.getPrimaryKey(edge.getStartNode()),
                              graph.getPrimaryKey(edge.getEndNode()),
                              edge.getType().ordinal(),
                              edge.getWeight());
            pw.println(line);
            count++;
            if (count % 100000 == 0)
                System.out.println("Exported " + count + " edges..");
            return null;
        }
    }

    /**
     * Serialized a node to a string.
     */
    protected abstract String serializeNode(NodeID<T> node);
}
