package recng.common.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import au.com.bytecode.opencsv.CSVReader;

/**
 * A cursor used when reading rows from a csv file.
 * 
 * @author jon
 * 
 */
class DefaultCSVCursor implements CSVCursor<String[]> {

    private final String fileName;
    private final CSVReader csvReader;
    private final List<Closeable> resources;
    private final CSVDescriptor descriptor;
    private int currentRow = 0;

    DefaultCSVCursor(String file, CSVDescriptor descriptor) throws IOException {
        this.fileName = file;
        this.descriptor = descriptor;
        this.resources = new ArrayList<Closeable>();
        CSVDialect dialect = descriptor.getDialect();
        String encoding = descriptor.getEncoding();
        Reader reader;
        if (descriptor.isGzipped()) {
            InputStream gzipStream =
                new GZIPInputStream(new FileInputStream(file));
            resources.add(gzipStream);
            reader = new BufferedReader(
                                        new InputStreamReader(gzipStream,
                                                              encoding));
        } else {
            InputStream fileStream = new FileInputStream(file);
            resources.add(fileStream);
            reader = new BufferedReader(
                                        new InputStreamReader(fileStream,
                                                              encoding));
        }
        resources.add(reader);
        this.csvReader = new CSVReader(reader, dialect.getSeparator(),
                                       dialect.getQuoteChar(),
                                       dialect.getEscapeChar());
        resources.add(csvReader);
        if (descriptor.hasHeader()) {
            String[] header = nextRow();
            descriptor.setColumns(Arrays.asList(header));
        }
    }

    @Override
    public String[] nextRow() throws IOException {
        currentRow++;
        return csvReader.readNext();
    }

    @Override
    public void close() throws IOException {
        for (Closeable resource : resources) {
            try {
                resource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<String> getColumnNames() {
        return descriptor.getColumns();
    }

    @Override
    public int currentRow() {
        return currentRow;
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}