package recng.recommendations.test;

import java.io.IOException;
import org.springframework.core.convert.converter.Converter;

import recng.common.TableMetadata;
import recng.common.TableMetadataUtils;
import recng.common.io.CSVCursor;
import recng.common.io.CSVDescriptor;
import recng.common.io.CSVDialect;
import recng.common.io.CSVUtils;
import recng.db.h2.H2KVStore;
import recng.graph.Graph;
import recng.graph.GraphBuilder;
import recng.graph.GraphImporter;
import recng.graph.GraphImporterImpl;
import recng.graph.GraphMetadata;
import recng.graph.ImmutableGraphImpl;
import recng.index.ID;
import recng.index.StringIDs;
import recng.profiling.ProfilerEntry;
import recng.profiling.ProfilerSettings;
import recng.profiling.ProfilingLevel;
import recng.recommendations.ProductQuery;
import recng.recommendations.ProductQueryImpl;
import recng.recommendations.RecommendationModel;
import recng.recommendations.RecommendationModelImpl;
import recng.recommendations.StringIDParser;
import recng.recommendations.data.ProductRepositoryImpl;
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.filter.ProductFilter;
import recng.recommendations.graph.RecommendationEdgeType;
import recng.recommendations.graph.RecommendationGraphMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

/**
 * Used to stress test {@link RecommendationModelImpl}. Should probably move
 * someplace else and definitaly stop using hard coded paths...
 *
 * @author jon
 *
 */
public class RecommendationModelTester {

    private List<String> clicks;
    private TableMetadata metadata;
    private final AtomicInteger current = new AtomicInteger();
    private RecommendationModel model;
    private Graph<ID<String>> graph;
    private ProductFilter filter;

    private static final String BASE_DIR = "/home/jon";
    private static final String CUSTOMER = "suomalainen";

    private void setup() throws IOException, MalformedObjectNameException,
        InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        System.out.println("Initiating model...");
        long t0 = System.currentTimeMillis();
        metadata =
            TableMetadataUtils
                .parseTableMetadata(getCustDir() + "/productformat.txt");
        this.clicks =
            getClicks(getCustDir() + "/clickdata", 100000);

        Map<String, String> config = new HashMap<String, String>();
        config.put("url",
                   String.format("%s/h2", getCustDir()));
        config.put("table", "products");
        config.put("user", "admin");
        config.put("pwd", "admin");
        config.put("primary_key", "__id");
        H2KVStore h2Store = new H2KVStore(metadata);
        h2Store.init(config);
        if (shouldImport()) {
            System.out.println("Importing product data...");
            long t2 = System.currentTimeMillis();
            int inserted =
                h2Store.importCSV(getCustDir() + "/productdata",
                                  new CSVDialect());
            long t3 = System.currentTimeMillis();
            System.out.println(String.format("Inserted %s rows in %s ms.",
                                             inserted, t3 - t2));
        }
        GraphMetadata graphMetadata = RecommendationGraphMetadata.getInstance();
        GraphBuilder<ID<String>> builder =
            ImmutableGraphImpl.Builder.create(graphMetadata);

        GraphImporter<ID<String>> importer =
            new GraphImporterImpl<ID<String>>(builder, graphMetadata,
                                              new Converter<String, ID<String>>() {

                                                  @Override
                                                  public ID<String> convert(String id) {
                                                      return StringIDs.parseID(id);
                                                  }
                                              });
        long t4 = System.currentTimeMillis();
        this.graph =
            importer.importGraph(getCustDir() + "/graph.csv");
        long t5 = System.currentTimeMillis();
        System.out.println("Imported graph in " + (t5 - t4) + " ms.");
        this.model =
            new RecommendationModelImpl<ID<String>>(
                                                    graph,
                                                    new ProductRepositoryImpl(
                                                                              h2Store,
                                                                              metadata),
                                                    new StringIDParser());
        model.setProfilerSettings(new ProfilerSettings(ProfilingLevel.SLOW, 50));
        this.filter = new ProductFilter() {

            @Override
            public boolean accepts(ImmutableProduct product) {
                return (Double) product.getProperty("Price") > 10;
            }
        };
        long t1 = System.currentTimeMillis();
        System.out.println("Model initiated in " + (t1 - t0) + " ms.");
    }

    private RecommendationModel getModel() {
        return model;
    }

    private boolean shouldImport() {
        return false;
    }

    private String getCustDir() {
        return String.format("%s/%s", BASE_DIR, CUSTOMER);
    }

    private List<ImmutableProduct>
        getRecommendations(String pid) {
        List<ImmutableProduct> pwb =
            model
                .getRelatedProducts(pid,
                                    getQuery(RecommendationEdgeType.PEOPLE_WHO_BOUGHT));
        List<ImmutableProduct> pwv =
            model
                .getRelatedProducts(pid,
                                    getQuery(RecommendationEdgeType.PEOPLE_WHO_VIEWED));
        List<ImmutableProduct> res = new ArrayList<ImmutableProduct>(pwb);
        res.addAll(pwv);
        return res;
    }

    private ProductQuery getQuery(RecommendationEdgeType type) {
        ProductQueryImpl query = new ProductQueryImpl(5, type);
        query.setMaxCursorSize(Integer.MAX_VALUE);
        query.setFilter(filter);
        return query;
    }

    private static List<String> getClicks(String clickFile, int limit)
        throws IOException {
        List<String> res = new ArrayList<String>();
        CSVDescriptor descriptor =
            new CSVDescriptor().setGzipped(clickFile.endsWith(".gz"));
        CSVCursor<String[]> cursor = CSVUtils.read(clickFile, descriptor);
        try {
            String[] row;
            String prev = null;
            while ((row = cursor.nextRow()) != null && res.size() < limit) {
                String pid = row[1];
                if (!pid.equals(prev))
                    res.add(pid);
                prev = pid;
            }
        } finally {
            cursor.close();
        }
        Collections.shuffle(res, new Random(0));
        return res;
    }

    private String getNextProduct() {
        int index = current.getAndIncrement();
        if (index >= clicks.size()) {
            index = 0;
            current.set(0);
        }
        return clicks.get(index);
    }

    public static void main(String[] args) throws IOException, MalformedObjectNameException,
        InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        final RecommendationModelTester tester = new RecommendationModelTester();
        tester.setup();
        int nThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(nThreads);
        long t0 = System.currentTimeMillis();
        int count = 500000;
        final AtomicLong maxLatency = new AtomicLong();
        final AtomicLong totLatency = new AtomicLong();
        final AtomicInteger slow = new AtomicInteger();
        final AtomicInteger recCount = new AtomicInteger();

        for (int i = 0; i < count; i++) {
            final int k = i;
            service.submit(new Runnable() {

                @Override
                public void run() {
                    if (k > 0 && k % 50000 == 0)
                        System.out.println("Done for " + k + " products");
                    String pid = tester.getNextProduct();
                    long t2 = System.currentTimeMillis();
                    List<ImmutableProduct> recs = tester.getRecommendations(pid);
                    recCount.addAndGet(recs.size());
                    long t3 = System.currentTimeMillis();
                    long delta = t3 - t2;
                    if (delta > maxLatency.get())
                        maxLatency.set(delta);
                    totLatency.addAndGet(delta);
                    if (delta > 100)
                        slow.incrementAndGet();
                }
            });
        }
        service.shutdown();
        try {
            service.awaitTermination(5, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long t1 = System.currentTimeMillis();
        System.out.println(tester.graph);
        System.out.println("Made " + count
            + " recommendations returning " + recCount.get() + " products in "
            + (t1 - t0) + " ms.");
        System.out.println(((1000l * count) / (t1 - t0))
            + " recommendations per second.");
        System.out.println("Max latency: " + maxLatency.get() + " ms.");
        System.out.println(String.format("Avg latency: %.0f ms",
                                         1d * totLatency.get() / count));
        System.out
            .println(String.format("Slow calls (> 100 ms): %s (%.3f %%)",
                                   slow.get(), 100d * slow.get() / count));
        int profiled = 0;
        for (Iterator<ProfilerEntry> it = tester.getModel().getProfilerEntries(); it.hasNext();) {
            System.out.println(it.next());
            profiled++;
        }
        System.out.println("Profiled (at least) " + profiled + " events.");
        System.in.read(); // Wait for input to abort
    }
}
