package recng.graph.visualize;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import recng.common.Consumer;
import recng.common.io.Closer;
import recng.graph.EdgeFilter;
import recng.graph.EdgeType;
import recng.graph.Graph;
import recng.graph.GraphBuilder;
import recng.graph.GraphCursor;
import recng.graph.GraphEdge;
import recng.graph.GraphExporter;
import recng.graph.GraphImporter;
import recng.graph.GraphImporterImpl;
import recng.graph.GraphMetadata;
import recng.graph.ImmutableGraphImpl;
import recng.graph.NodeID;
import recng.graph.NodeType;
import recng.graph.Traverser;
import recng.index.ID;
import recng.index.StringIDs;
import recng.recommendations.domain.RecommendationNodeType;
import recng.recommendations.graph.RecommendationGraphMetadata;

/**
 * Exports a graph to a text file in graphviz format.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the node IDs.
 */
public class GraphVizExporter<T> implements GraphExporter<T> {

    private Set<NodeID<T>> sources = null;
    private int maxDepth = Integer.MAX_VALUE;
    private int maxEdges = Integer.MAX_VALUE;
    private int maxNodes = Integer.MAX_VALUE;
    private int maxEdgesPerNode = Integer.MAX_VALUE;

    public void setSources(Set<NodeID<T>> sources) {
        this.sources = sources;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setMaxEdges(int maxEdges) {
        this.maxEdges = maxEdges;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public void setMaxEdgesPerNode(int maxEdgesPerNode) {
        this.maxEdgesPerNode = maxEdgesPerNode;
    }

    private void writeFullGraph(Graph<T> graph,
                                final GraphVizWriter<T> graphWriter) {
        graph.getAllNodes(new Consumer<NodeID<T>, Void>() {
            @Override
            public Void consume(NodeID<T> node) {
                graphWriter.writeNode(node);
                return null;
            }
        });

        graph.getAllEdges(new Consumer<GraphEdge<T>, Void>() {

            @Override
            public Void consume(GraphEdge<T> edge) {
                graphWriter.writeEdge(edge);
                return null;
            }
        });
    }

    private void writeSubGraph(Graph<T> graph,
                               final GraphVizWriter<T> graphWriter) {
        final Set<NodeID<T>> visitedNodes = new HashSet<NodeID<T>>();
        int edgeCount = 0;

        TObjectIntMap<NodeID<T>> outEdgeCounts =
            new TObjectIntHashMap<NodeID<T>>();
        outer: for (NodeID<T> node : sources) {
            graphWriter.writeNode(node);
            visitedNodes.add(node);
            for (EdgeType eType : graph.getMetadata().getEdgeTypes()) {
                Traverser<T> traverser =
                    graph.getTraverser(node, eType);
                EdgeFilter<T> filter = new EdgeFilter<T>() {

                    @Override
                    public boolean accepts(NodeID<T> startNode,
                                           NodeID<T> endNode) {
                        return visitedNodes.contains(startNode);
                    }
                };
                traverser.setMaxDepth(maxDepth)
                    .setReturnableFilter(filter);
                GraphCursor<T> cursor = traverser.traverse();
                try {
                    while (cursor.hasNext()) {
                        GraphEdge<T> edge = cursor.next();
                        NodeID<T> startNode = edge.getStartNode();
                        if (!outEdgeCounts.containsKey(startNode))
                            outEdgeCounts.put(startNode, 0);

                        if (outEdgeCounts.get(startNode) <= maxEdgesPerNode) {
                            outEdgeCounts.increment(startNode);
                            graphWriter.writeEdge(edge);
                            visitedNodes.add(startNode);
                            NodeID<T> endNode = edge.getEndNode();
                            visitedNodes.add(endNode);
                            if (++edgeCount >= maxEdges)
                                break outer;
                        }
                        if (visitedNodes.size() >= maxNodes)
                            break outer;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
    }

    @Override
    public void exportGraph(Graph<T> graph, String file) {
        PrintWriter writer = null;
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            writer = new PrintWriter(bw);
            writer.println("digraph G {");
            final GraphVizWriter<T> graphWriter =
                new GraphVizWriter<T>(graph.getMetadata(), writer);

            if (sources == null || sources.isEmpty()) {
                writeFullGraph(graph, graphWriter);
            } else {
                writeSubGraph(graph, graphWriter);
            }
            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closer.close(bw, writer);
        }
        System.out.println("GraphViz file exported.");
    }

    private static class GraphVizWriter<T> {

        private final Map<NodeType, String> nodeShapes;
        private final Map<EdgeType, String> edgeColors;
        private final PrintWriter writer;

        public GraphVizWriter(GraphMetadata metadata, PrintWriter writer) {
            this.writer = writer;

            // Set up the shapes to use for each node type
            this.nodeShapes = new HashMap<NodeType, String>();
            int ntIndex = 0;
            int nShapes = GraphVizConstants.SHAPES.length;
            for (NodeType nodeType : metadata.getNodeTypes())
                nodeShapes.put(nodeType, GraphVizConstants.SHAPES[ntIndex++
                    % nShapes]);

            // Set up the edge colors to use for each edge type
            this.edgeColors = new HashMap<EdgeType, String>();
            int nColors = GraphVizConstants.COLORS.length;
            int etIndex = 0;
            for (EdgeType edgeType : metadata.getEdgeTypes())
                edgeColors.put(edgeType,
                               GraphVizConstants.COLORS[etIndex++ % nColors]);
        }

        public void writeNode(NodeID<T> node) {
            String id = node.getID().toString();
            NodeType nodeType = node.getNodeType();
            writer.println(String.format("\"%s\" [shape=\"%s\"];",
                                         id, nodeShapes.get(nodeType)));
        }

        public void writeEdge(GraphEdge<T> edge) {
            String from = edge.getStartNode().getID().toString();
            String to = edge.getEndNode().getID().toString();
            float weight = edge.getWeight();
            EdgeType edgeType = edge.getType();
            if (edgeType.isWeighted())
                writer
                    .println(String
                        .format("\"%s\"->\"%s\" [label=\"t:%s\\nw:%.5f\" color=\"%s\" fontsize=12];",
                                from, to, edgeType.name(), weight,
                                edgeColors.get(edgeType)));
            else
                writer
                    .println(String
                        .format("\"%s\"->\"%s\" [label=\"t:%s\" color=\"%s\" fontsize=12];",
                                from, to, edgeType.name(),
                                edgeColors.get(edgeType)));

        }
    }

    public static void main(String[] args) {
        GraphMetadata metadata = RecommendationGraphMetadata.getInstance();
        GraphBuilder<ID<String>> builder =
            ImmutableGraphImpl.Builder.create(metadata);
        GraphImporter<ID<String>> importer =
            new GraphImporterImpl<ID<String>>(builder, metadata) {

                @Override
                protected ID<String> parseNodeID(String id) {
                    return StringIDs.parseID(id);
                }
            };
        Graph<ID<String>> graph = importer.importGraph(args[0]);
        Set<NodeID<ID<String>>> sources = new HashSet<NodeID<ID<String>>>();
        for (int i = 2; i < args.length; i++)
            sources.add(new NodeID<ID<String>>(StringIDs.parseID(args[i]),
                                               RecommendationNodeType.PRODUCT));
        GraphVizExporter<ID<String>> graphVizExporter =
            new GraphVizExporter<ID<String>>();
        graphVizExporter.setSources(sources);
        graphVizExporter.exportGraph(graph, args[1]);
    }
}