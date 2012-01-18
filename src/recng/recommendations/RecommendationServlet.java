package recng.recommendations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import recng.common.TableMetadata;
import recng.graph.Graph;
import recng.graph.GraphBuilder;
import recng.graph.GraphImporter;
import recng.graph.GraphImporterImpl;
import recng.graph.GraphMetadata;
import recng.graph.ImmutableGraphImpl;
import recng.graph.NodeID;
import recng.graph.NodeType;
import recng.index.ID;
import recng.index.StringIDs;
import recng.recommendations.data.DataStore;
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.graph.ProductID;
import recng.recommendations.graph.RecommendationGraphMetadata;
import tests.misc.TestMongoDataUploader;

public class RecommendationServlet extends HttpServlet {

    private static final long serialVersionUID = 22120115L;

    private RecommendationManager manager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            Enumeration<?> pNames = config.getInitParameterNames();
            while (pNames.hasMoreElements()) {
                String pName = (String) pNames.nextElement();
                parameters.put(pName, config.getInitParameter(pName));
            }
            this.manager =
                new RecommendationManagerImpl(setupModel(parameters));
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void
        doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        List<ImmutableProduct> recommendation =
            getRecommendation(getParameters(req));
        String response = getResponse(recommendation);
        PrintWriter out = res.getWriter();
        try {
            out.println(response);
        } finally {
            out.close();
        }
    }

    private Map<String, String> getParameters(HttpServletRequest req)
        throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        Enumeration<?> pNames = req.getParameterNames();
        while (pNames.hasMoreElements()) {
            String pName = (String) pNames.nextElement();
            params.put(pName, req.getParameter(pName));
        }
        return params;
    }

    private String getResponse(List<ImmutableProduct> recommendation) {
        // TODO: Get real response (JSON)
        return recommendation == null ? "Null" : recommendation.toString();
    }

    private RecommendationModel setupModel(Map<String, String> config)
        throws IOException {
        return new RecommendationModelImpl<ID<String>>(setupGraph(config),
                                                       setupDataStore(config),
                                                       new StringIDFactory());
    }

    private Graph<ID<String>> setupGraph(Map<String, String> config)
        throws IOException {
        GraphMetadata metadata = RecommendationGraphMetadata.getInstance();
        GraphBuilder<ID<String>> builder =
            new ImmutableGraphImpl.Builder<ID<String>>(metadata);
        GraphImporter<ID<String>> importer =
            new GraphImporterImpl<ID<String>>(builder, metadata) {

                @Override
                protected NodeID<ID<String>> getNodeKey(String id,
                                                        NodeType nodeType) {
                    return new ProductID<ID<String>>(StringIDs.parseKey(id));
                }
            };
        long t0 = System.currentTimeMillis();
        String file = config.get("graph_file");
        Graph<ID<String>> graph = importer.importGraph(file);
        long t1 = System.currentTimeMillis();
        System.out.println("Done with graph import. Imported "
            + graph.nodeCount()
            + " nodes and " + graph.edgeCount() + " edges in " +
            (t1 - t0) + " ms.");

        return null;
    }

    private DataStore setupDataStore(Map<String, String> config)
        throws IOException {
        TableMetadata metadata = readTableMetadata(config.get("metadata_file"));
        return null;
    }

    private TableMetadata readTableMetadata(String file) throws IOException {
        return TestMongoDataUploader.parseTableMetadata(file);
    }

    private
        List<ImmutableProduct>
        getRecommendation(Map<String, String> params) {
        return manager.getRelatedProducts(params);
    }
}
