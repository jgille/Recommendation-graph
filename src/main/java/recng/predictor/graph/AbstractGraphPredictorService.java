package recng.predictor.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import recng.common.BinPropertyContainer;
import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.PropertyContainer;
import recng.common.PropertyContainerFactory;
import recng.common.TableMetadata;
import recng.common.TableMetadataImpl;
import recng.common.io.CSVDescriptor;
import recng.common.io.CSVPropertyCursor;
import recng.common.io.CSVUtils;
import recng.graph.Graph;
import recng.predictor.PredictorBaseData;

/**
 * Base prediction service producing a graph.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the graph node keys.
 */
public abstract class AbstractGraphPredictorService<T> implements
    GraphPredictorService<T> {

    private String clickDataFile, transactionDataFile;
    private boolean filterDuplicateNeighbors = true;

    @Override
    public void init(Map<String, String> config) {
        this.clickDataFile = config.get("click_data_file");
        this.transactionDataFile = config.get("transaction_data_file");

        if (clickDataFile == null && transactionDataFile == null)
            throw new IllegalArgumentException("At least one of " +
                "\"click_data_file\" or \"transaction_data_file\"" +
                "must be specified in the config");

        if (config.containsKey("filter_duplicate_neighbors"))
            this.filterDuplicateNeighbors =
                config.get("filter_duplicate_neighbors")
                    .equalsIgnoreCase("true");
    }

    @Override
    public Graph<T> createPredictions() {
        try {
            GraphPredictorBaseData baseData =
                new GraphPredictorBaseData(filterDuplicateNeighbors);
            addTransactionData(baseData);
            addClickData(baseData);
            Graph<T> recGraph =
                createPredictions(baseData);
            return recGraph;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Performs the predictions based on the provided base data.
     */
    protected abstract Graph<T> createPredictions(PredictorBaseData baseData);

    private void addTransactionData(GraphPredictorBaseData baseData)
        throws IOException {
        CSVPropertyCursor cursor =
            setupCursor(transactionDataFile,
                        Arrays.asList("UserID", "ProductID", "OrderID"), 2);
        int c = 0;
        try {
            PropertyContainer props = null;
            while ((props = cursor.nextRow()) != null) {
                if (c > 0 && c % 100000 == 0) {
                    System.out.println("Processed " + c + " transaction rows");
                }
                c++;
                String userID = (String) props.getProperty("UserID");
                String productID = (String) props.getProperty("ProductID");
                baseData.addPurchasedProduct(userID, productID);
            }
        } finally {
            cursor.close();
        }
        System.out.println("Processed " + c + " transaction rows");
    }

    private void addClickData(GraphPredictorBaseData baseData)
        throws IOException {
        CSVPropertyCursor cursor =
            setupCursor(clickDataFile,
                        Arrays.asList("SessionID", "ProductID"), 2);
        int c = 0;
        try {
            PropertyContainer props = null;
            while ((props = cursor.nextRow()) != null) {
                if (c > 0 && c % 100000 == 0) {
                    System.out.println("Processed " + c + " click data rows");
                }
                c++;
                String sessionID = (String) props.getProperty("SessionID");
                String productID = (String) props.getProperty("ProductID");
                baseData.addViewedProduct(sessionID, productID);
            }
        } finally {
            cursor.close();
        }
        System.out.println("Processed " + c + " click data rows");
    }

    private CSVPropertyCursor setupCursor(String file, List<String> columns,
                                          int minColumns)
        throws IOException {
        CSVDescriptor descriptor = new CSVDescriptor();
        boolean isGzipped = file.endsWith(".gz");
        descriptor.setColumns(columns).setGzipped(isGzipped);
        List<FieldMetadata> fieldMetadata =
            getFieldMetadata(descriptor.getColumns());
        TableMetadata metadata = new TableMetadataImpl(fieldMetadata);
        descriptor.setMetadata(metadata);
        descriptor.setMinColumns(minColumns);
        PropertyContainerFactory propFactory =
            new BinPropertyContainer.Factory(false);
        CSVPropertyCursor cursor =
            CSVUtils.readAndParse(file, descriptor, propFactory);
        return cursor;
    }

    private List<FieldMetadata> getFieldMetadata(List<String> columns) {
        List<FieldMetadata> metadata = new ArrayList<FieldMetadata>();
        for (String column : columns)
            metadata.add(FieldMetadataImpl.create(column,
                                                  FieldMetadata.Type.STRING));
        return metadata;
    }
}
