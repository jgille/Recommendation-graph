package rectest.recommendations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rectest.graph.EdgeType;
import rectest.graph.Graph;
import rectest.graph.GraphBuilder;
import rectest.graph.GraphImpl;
import rectest.graph.NodeId;
import rectest.graph.NodeType;
import rectest.graph.NodeTypeImpl;
import rectest.index.Key;

public class PredictorImpl implements Predictor {

    private static final Set<EdgeType> EDGE_TYPES =
        new HashSet<EdgeType>(EnumSet.allOf(RecommendationType.class));

    private static final NodeType NODE_TYPE = new NodeTypeImpl("Product",
                                                               EDGE_TYPES);

    public Graph<Key<String>> setupPredictions(String npDataFile,
                                               String clickDataFile,
                                               KeyParser<Key<String>> pip) {

        GraphBuilder<Key<String>> builder =
            new GraphImpl.Builder<Key<String>>();

        Map<String, Key<String>> cachedKeys =
            new HashMap<String, Key<String>>();
        try {
            BidiMaps npData = readNPData(npDataFile, cachedKeys, pip);
            System.out.println("**** Created " + npData.relationCount() +
                " np-data relations");
            BidiMaps clickData = readClickData(clickDataFile, cachedKeys, pip);
            System.out.println("**** Created " + clickData.relationCount() +
                " click-data relations");

            cachedKeys.clear();

            addRelations(builder, pip, npData,
                         RecommendationType.PEOPLE_WHO_BOUGHT);
            npData.clear();
            addRelations(builder, pip, clickData,
                         RecommendationType.PEOPLE_WHO_VIEWED);
            clickData.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    private void addRelations(GraphBuilder<Key<String>> builder,
                              KeyParser<Key<String>> pip,
                              BidiMaps maps,
                              RecommendationType recommendationType) {
        int i = 0;
        System.out.println("Creating " + recommendationType + " relations...");
        int j = 0;
        for (Key<String> product : maps.valueSet()) {
            Map<Key<String>, Integer> productCounts =
                new HashMap<Key<String>, Integer>();
            Set<Key<String>> keys = maps.getKeys(product);
            if (keys == null)
                continue;
            for (Key<String> key : keys) {
                Set<Key<String>> products = maps.getValues(key);
                if (products == null)
                    continue;
                for (Key<String> otherProduct : products) {
                    if (otherProduct.equals(product))
                        continue;
                    int count = productCounts.containsKey(otherProduct) ?
                        productCounts.get(otherProduct).intValue() : 0;
                    productCounts.put(otherProduct, count + 1);
                }
            }
            for (Map.Entry<Key<String>, Integer> e : productCounts.entrySet()) {
                Key<String> otherProduct = e.getKey();
                int count = e.getValue();
                if (count < 2)
                    continue;
                if (i++ % 100000 == 0)
                    System.out.println("Added " + (i - 1) + " relations");
                // TODO: Normalize weight?
                builder
                    .addEdge(new NodeId<Key<String>>(product, NODE_TYPE),
                             new NodeId<Key<String>>(otherProduct, NODE_TYPE),
                             recommendationType,
                                count);
            }
            j++;
            if (j % 10000 == 0)
                System.out.println("Added " + j + " products");
        }
    }

    private static BidiMaps readNPData(String file,
                                       Map<String, Key<String>> cachedKeys,
                                       KeyParser<Key<String>> pip)
        throws IOException {
        FileReader fr = null;
        BufferedReader br = null;
        BidiMaps maps = new BidiMaps(cachedKeys, pip);
        System.out.println("Reading NP data...");
        int i = 0;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";");
                if (fields.length < 2) {
                    System.out.println(
                        "Invalid number of fields ("
                            + fields.length
                            + ") for line: "
                            + line);
                    continue;
                }
                String user = fields[0].replaceAll("\"", "");
                String product = fields[1].replaceAll("\"", "");
                maps.add(user, product);
                if ((i++ % 1000000 == 0))
                    System.out.println("Done for " + (i - 1) + " rows");
            }
        } finally {
            if (br != null)
                br.close();
            if (fr != null)
                fr.close();
        }
        return maps;
    }

    private static BidiMaps readClickData(String file,
                                          Map<String, Key<String>> cachedKeys,
                                          KeyParser<Key<String>> pip)
        throws IOException {
        FileReader fr = null;
        BufferedReader br = null;
        BidiMaps maps = new BidiMaps(cachedKeys, pip);
        System.out.println("Reading click data...");
        int i = 0;

        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";");
                if (fields.length < 2) {
                    System.out.println(
                        "Invalid number of fields ("
                            + fields.length
                            + ") for line: "
                            + line);
                    continue;
                }
                String session = fields[0].replaceAll("\"", "");
                String product = fields[1].replaceAll("\"", "");
                maps.add(session, product);
                if ((i++ % 1000000 == 0))
                    System.out.println("Done for " + (i - 1) + " rows");
            }
        } finally {
            if (br != null)
                br.close();
            if (fr != null)
                fr.close();
        }
        return maps;
    }

    private static class BidiMaps {
        private final Map<Key<String>, Set<Key<String>>> map =
            new HashMap<Key<String>, Set<Key<String>>>();

        private final Map<Key<String>, Set<Key<String>>> reverseMap =
            new HashMap<Key<String>, Set<Key<String>>>();

        private final Map<String, Key<String>> cachedKeys;
        private final KeyParser<Key<String>> pip;

        public BidiMaps(Map<String, Key<String>> cachedKeys,
                        KeyParser<Key<String>> pip) {
            this.cachedKeys = cachedKeys;
            this.pip = pip;
        }

        public void clear() {
            map.clear();
            reverseMap.clear();
        }

        public void add(String key, String value) {
            Key<String> k = cachedKeys.get(key);
            if (k == null) {
                k = pip.parseKey(key);
                cachedKeys.put(key, k);
            }
            Key<String> v = cachedKeys.get(value);
            if (v == null) {
                v = pip.parseKey(value);
                cachedKeys.put(value, v);
            }
            add(map, k, v);
            add(reverseMap, v, k);
        }

        private void
            add(Map<Key<String>, Set<Key<String>>> m, Key<String> key,
                Key<String> value) {
            Set<Key<String>> values = m.get(key);
            if (values == null) {
                values = new HashSet<Key<String>>();
                m.put(key, values);
            }

            values.add(value);
        }

        public int relationCount() {
            int rc = 0;
            for (Map.Entry<Key<String>, Set<Key<String>>> e : map.entrySet()) {
                rc += e.getValue().size();
            }
            return rc;
        }

        public Set<Key<String>> getValues(Key<String> key) {
            return map.get(key);
        }

        public Set<Key<String>> getKeys(Key<String> value) {
            return reverseMap.get(value);
        }

        public Set<Key<String>> valueSet() {
            return reverseMap.keySet();
        }
    }
}
