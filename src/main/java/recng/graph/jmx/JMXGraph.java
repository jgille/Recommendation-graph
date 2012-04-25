package recng.graph.jmx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import recng.graph.EdgeType;
import recng.graph.Graph;
import recng.graph.GraphStats;
import recng.graph.NodeType;
import recng.jmx.AbstractMBean;

public class JMXGraph<T> extends AbstractMBean implements JMXGraphMBean {

    private final Graph<T> graph;

    public JMXGraph(Graph<T> graph) {
        this.graph = graph;
        setBeanName(graph.getClass().getName());
    }

    @Override
    public int getNodeCount() {
        return graph.nodeCount();
    }

    @Override
    public int getEdgeCount() {
        return graph.edgeCount();
    }

    @Override
    public List<String> getNodeTypes() {
        List<String> res = new ArrayList<String>();
        for (NodeType nodeType : graph.getMetadata().getNodeTypes())
            res.add(nodeType.name());
        return res;
    }

    @Override
    public List<String> getEdgeTypes() {
        List<String> res = new ArrayList<String>();
        for (EdgeType edgeType : graph.getMetadata().getEdgeTypes())
            res.add(edgeType.name());
        return res;
    }

    @Override
    public Date getInitTime() {
        GraphStats stats = graph.getStats();
        return stats.getInitTime();
    }

    @Override
    public int getTraversals() {
        GraphStats stats = graph.getStats();
        return stats.getTraversals();
    }

    @Override
    public int getTraversedEdges() {
        GraphStats stats = graph.getStats();
        return stats.getTraversedEdges();
    }

    @Override
    public int getMaxTraversedEdges() {
        GraphStats stats = graph.getStats();
        return stats.getMaxTraversedEdges();
    }

    @Override
    public int getAverageTraversedEdges() {
        GraphStats stats = graph.getStats();
        int traversed = stats.getTraversedEdges();
        int traversals = stats.getTraversals();
        if (traversals == 0)
            return 0;
        return (int) Math.round(1d * traversed / traversals);
    }
}
