package recng.common.io;

import java.util.List;

import recng.common.TableMetadata;

/**
 * Describes a csv file.
 * 
 * @author jon
 * 
 */
public class CSVDescriptor {
    private String encoding;
    private boolean gzipped;
    private CSVDialect dialect;
    private boolean hasHeader;
    private List<String> columns;
    private TableMetadata metadata;
    private int minColumns;

    public CSVDescriptor() {
        this.encoding = "UTF8";
        this.gzipped = false;
        this.dialect = new CSVDialect();
        this.hasHeader = false;
        this.minColumns = 0;
    }

    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the encoding of the file. Defaults to UTF8.
     */
    public CSVDescriptor setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public boolean isGzipped() {
        return gzipped;
    }

    /**
     * Sets the gzipped flag, indicating if the csv is gzipped or not.
     * 
     * Defaults to false.
     */
    public CSVDescriptor setGzipped(boolean gzipped) {
        this.gzipped = gzipped;
        return this;
    }

    public CSVDialect getDialect() {
        return dialect;
    }

    /**
     * Sets the {@link CSVDialect} used in the csv file.
     * 
     * For defaults, see {@link CSVDialect}.
     */
    public CSVDescriptor setDialect(CSVDialect dialect) {
        this.dialect = dialect;
        return this;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    /**
     * Indicated wheter or not the csv file contains a header row.
     * 
     * Defaults to false.
     */
    public CSVDescriptor setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
        return this;
    }

    public List<String> getColumns() {
        return columns;
    }

    /**
     * Gets the column names in the csv file, gotten from the file header if it
     * exists.
     * 
     * Defaults to null, i.e. unknown.
     */
    public CSVDescriptor setColumns(List<String> fields) {
        this.columns = fields;
        return this;
    }

    public TableMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata for the columns in the file.
     * 
     * Defaults to null, i.e. unknown.
     */
    public CSVDescriptor setMetadata(TableMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Sets the minimum number of columns expected.
     * 
     * Defaults to 0.
     */
    public CSVDescriptor setMinColumns(int minColumns) {
        this.minColumns = minColumns;
        return this;
    }

    /**
     * Gets the minimum number of columns expected.
     */
    public int getMinColumns() {
        return minColumns;
    }
}