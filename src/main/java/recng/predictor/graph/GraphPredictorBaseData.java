package recng.predictor.graph;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import gnu.trove.TIntCollection;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import recng.predictor.PredictorBaseData;
import recng.recommendations.domain.RecommendationNodeType;

/**
 * Prediction base data stored in a graph.
 *
 * @author jon
 *
 */
public class GraphPredictorBaseData implements PredictorBaseData {

    private final TObjectIntHashMap<String> productIndex;
    private final TObjectIntHashMap<String> userIndex;
    private final TObjectIntHashMap<String> sessionIndex;
    private final List<Node> nodes;
    private final boolean filterDuplicates;

    /**
     * Constructs an empty {@link GraphPredictorBaseData}.
     *
     * @param filterDuplicates
     *            True if duplicated bought/viewed products for a user/session
     *            should be ignored.
     */
    public GraphPredictorBaseData(boolean filterDuplicates) {
        this.filterDuplicates = filterDuplicates;
        this.productIndex = new TObjectIntHashMap<String>(10000);
        this.userIndex = new TObjectIntHashMap<String>(50000);
        this.sessionIndex = new TObjectIntHashMap<String>(50000);
        this.nodes = new ArrayList<Node>(100000);
    }

    @Override public String toString() {
        return String.format("%s, %s, %s", productIndex.size(),
                             userIndex.size(), sessionIndex.size());
    }

    @Override
    public TIntCollection getAllProducts() {
        return productIndex.valueCollection();
    }

    @Override
    public TIntCollection getBuyers(int productID) {
        Node node = getNode(productID);
        return node.getNeighbors(RecommendationNodeType.USER);
    }

    @Override
    public TIntCollection getViewers(int productID) {
        Node node = getNode(productID);
        return node.getNeighbors(RecommendationNodeType.SESSION);
    }

    @Override
    public TIntCollection getPurchasedProducts(int userID) {
        Node node = getNode(userID);
        return node.getNeighbors(RecommendationNodeType.PRODUCT);
    }

    @Override
    public TIntCollection getViewedProducts(int sessionID) {
        Node node = getNode(sessionID);
        return node.getNeighbors(RecommendationNodeType.PRODUCT);
    }

    @Override
    public String getProductID(int index) {
        Node node = getNode(index);
        if (node.getType() != RecommendationNodeType.PRODUCT)
            throw new IllegalArgumentException(node + " is not a product node!");
        return node.getID();
    }

    @Override
    public String getUserID(int index) {
        Node node = getNode(index);
        if (node.getType() != RecommendationNodeType.USER)
            throw new IllegalArgumentException(node + " is not a user node!");
        return node.getID();
    }

    @Override
    public String getSessionID(int index) {
        Node node = getNode(index);
        if (node.getType() != RecommendationNodeType.SESSION)
            throw new IllegalArgumentException(node + " is not a session node!");
        return node.getID();
    }

    public int getIndex(RecommendationNodeType type, String id) {
        switch (type) {
        case PRODUCT:
            return productIndex.get(id);
        case USER:
            return userIndex.get(id);
        case SESSION:
            return sessionIndex.get(id);
        default:
            throw new IllegalArgumentException("illegal type: " + type);
        }
    }

    public synchronized void addPurchasedProduct(String user, String product) {
        int userID = addOrGetUser(user);
        int productID = addOrGetProduct(product);
        Node userNode = nodes.get(userID);
        Node productNode = nodes.get(productID);
        userNode.addNeighbor(productID, RecommendationNodeType.PRODUCT);
        productNode.addNeighbor(userID, RecommendationNodeType.USER);
    }

    public void addViewedProduct(String session, String product) {
        int sessionID = addOrGetSession(session);
        int productID = addOrGetProduct(product);
        Node sessionNode = nodes.get(sessionID);
        Node productNode = nodes.get(productID);
        sessionNode.addNeighbor(productID, RecommendationNodeType.PRODUCT);
        productNode.addNeighbor(sessionID, RecommendationNodeType.SESSION);
    }

    private int addOrGetProduct(String product) {
        if (!productIndex.contains(product)) {
            int index = addNode(RecommendationNodeType.PRODUCT, product);
            productIndex.put(product, index);
            return index;
        }
        return productIndex.get(product);
    }

    private int addOrGetUser(String user) {
        if (!userIndex.contains(user)) {
            int index = addNode(RecommendationNodeType.USER, user);
            userIndex.put(user, index);
            return index;
        }
        return userIndex.get(user);
    }

    private int addOrGetSession(String session) {
        if (!sessionIndex.contains(session)) {
            int index = addNode(RecommendationNodeType.SESSION, session);
            sessionIndex.put(session, index);
            return index;
        }
        return sessionIndex.get(session);
    }

    private synchronized int addNode(RecommendationNodeType type, String id) {
        Node node = new Node(type, id, filterDuplicates);
        int index = nodes.size();
        nodes.add(node);
        return index;
    }

    private Node getNode(int index) {
        if (index < 0 || index >= nodes.size())
            throw new IllegalArgumentException("Illegal index: " + index);
        return nodes.get(index);
    }

    private static class Node {

        private final RecommendationNodeType type;
        private final String id;
        private final EnumMap<RecommendationNodeType, TIntCollection> edges;
        private final boolean filterDuplicateNeighbors;

        public Node(RecommendationNodeType type, String id, boolean filterDuplicateNeighbors) {
            this.type = type;
            this.id = id;
            this.filterDuplicateNeighbors = filterDuplicateNeighbors;
            Class<RecommendationNodeType> cls = RecommendationNodeType.class;
            this.edges =
                new EnumMap<RecommendationNodeType, TIntCollection>(cls);
        }

        public TIntCollection getNeighbors(RecommendationNodeType neighborType) {
            return edges.get(neighborType);
        }

        public void addNeighbor(int index, RecommendationNodeType neighborType) {
            TIntCollection neighbors = edges.get(neighborType);
            if (neighbors == null) {
                neighbors =
                    filterDuplicateNeighbors ? new TIntHashSet()
                        : new TIntArrayList();
                edges.put(neighborType, neighbors);
            }
            neighbors.add(index);
        }

        public String getID() {
            return id;
        }

        public RecommendationNodeType getType() {
            return type;
        }

        @Override
        public String toString() {
            return String.format("%s: %s", type, id);
        }
    }
}