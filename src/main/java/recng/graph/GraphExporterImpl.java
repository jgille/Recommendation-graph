package recng.graph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
                graph.forEachNode(nodeExporter);
                System.out.println("Done exporting all nodes");
                pw.println("# Edges (start node index;end node index;" +
                    "edge type ordinal;edge weight)");
                EdgeExporter<T> edgeExporter = new EdgeExporter<T>(graph, pw);
                graph.forEachEdge(edgeExporter);
                System.out.println("Done exporting all edges");
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

    private class NodeExporter implements NodeIDProcedure<T> {

        private final Graph<T> graph;
        private final int nodeCount;
        private final PrintWriter pw;
        private int count = 0;

        public NodeExporter(Graph<T> graph, PrintWriter pw) {
            this.graph = graph;
            this.pw = pw;
            this.nodeCount = graph.nodeCount();
        }

        @Override
        public boolean apply(NodeID<T> node) {
            String line =
                String.format("%s;%s;%s",
                              graph.getPrimaryKey(node),
                              GraphExporterImpl.this.serializeNodeID(node
                                  .getID()),
                              node.getNodeType().ordinal());
            pw.println(line);
            count++;
            if (count % 10000 == 0) {
                System.out.println(String.format("Exported %s of %s nodes..",
                                                 count, nodeCount));

            }
            return true;
        }
    }

    private static class EdgeExporter<T> implements GraphEdgeProcedure<T> {

        private final Graph<T> graph;
        private final int edgeCount;
        private final PrintWriter pw;
        private int count = 0;

        public EdgeExporter(Graph<T> graph, PrintWriter pw) {
            this.graph = graph;
            this.pw = pw;
            this.edgeCount = graph.edgeCount();
        }

        @Override
        public boolean apply(GraphEdge<T> edge) {
            String line =
                String.format("%s;%s;%s;%s",
                              graph.getPrimaryKey(edge.getStartNode()),
                              graph.getPrimaryKey(edge.getEndNode()),
                              edge.getType().ordinal(),
                              edge.getWeight());
            pw.println(line);
            count++;
            if (count % 1000000 == 0)
                System.out.println(String.format("Exported %s of %s edges..",
                                                 count, edgeCount));
            return true;
        }
    }

    /**
     * Serialized a node to a string.
     */
    protected abstract String serializeNodeID(T nodeID);
}
