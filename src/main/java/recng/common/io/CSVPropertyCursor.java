package recng.common.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import recng.common.FieldMetadata;
import recng.common.Marshaller;
import recng.common.PropertyContainer;
import recng.common.PropertyContainerFactory;
import recng.common.TableMetadata;

/**
 * A csv file cursor that parses each row in a {@link PropertyContainer}.
 * 
 * @author jon
 * 
 */
public class CSVPropertyCursor implements CSVCursor<PropertyContainer> {

    private final CSVCursor<String[]> rows;
    private final CSVDescriptor descriptor;
    private final List<String> columns;
    private final PropertyContainerFactory factory;

    CSVPropertyCursor(CSVCursor<String[]> rows,
                      CSVDescriptor descriptor,
                      PropertyContainerFactory factory) {
        this.rows = rows;
        this.factory = factory;
        if (descriptor.getMetadata() == null)
            throw new IllegalArgumentException("Metadata must be set");
        this.descriptor = descriptor;
        if (descriptor.getColumns() == null
            || descriptor.getColumns().isEmpty()) {
            this.columns = getFieldsFromMetadata();
        } else {
            this.columns = descriptor.getColumns();
        }
    }

    private List<String> getFieldsFromMetadata() {
        List<String> res = new ArrayList<String>();
        for (String field : descriptor.getMetadata().getFields())
            res.add(field);
        return res;
    }

    private PropertyContainer parseRow(String[] row)
        throws InvalidCSVRowException {
        TableMetadata metadata = descriptor.getMetadata();
        PropertyContainer props = factory.create(metadata);
        if (row.length == 0)
            return null; // Ignore empty rows
        else if (row[0].startsWith("#"))
            return null; // Ignore commented rows
        if (row.length > columns.size())
            throw new InvalidCSVRowException("Too many columns " +
                "columns for line number: " + rows.currentRow() +
                ", row: " + Arrays.asList(row) + ", expected columns: " +
                columns);
        if (row.length < descriptor.getMinColumns()) {
            throw new InvalidCSVRowException("Too few columns " +
                "columns for line number: " + rows.currentRow() +
                ", row: " + Arrays.asList(row) + ", expected columns: " +
                columns.subList(0, descriptor.getMinColumns()));
        }

        int i = 0;
        for (String cell : row) {
            String fieldName = columns.get(i++);
            FieldMetadata fieldMetadata =
                metadata.getFieldMetadata(fieldName);
            Marshaller marshaller = fieldMetadata.getMarshaller();
            Object value = marshaller.parse(cell);
            props.setProperty(fieldName, value);
        }
        return props;
    }

    @Override
    public PropertyContainer nextRow() throws IOException {
        String[] next = rows.nextRow();
        if (next == null)
            return null;
        return parseRow(next);
    }

    @Override
    public void close() throws IOException {
        rows.close();
    }

    @Override
    public List<String> getColumnNames() {
        return rows.getColumnNames();
    }

    @Override
    public int currentRow() {
        return rows.currentRow();
    }

    @Override
    public String getFileName() {
        return rows.getFileName();
    }
}