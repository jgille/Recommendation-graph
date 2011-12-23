package recng.recommendations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import recng.common.Consumer;

/**
 * Base class used to read and parse product data from file. Override the method
 * consume(Map<String, Object>) to handle the parsed product data.
 *
 * @author jon
 *
 */
public abstract class AbstractProductDataReader
    implements ProductDataReader, Consumer<Map<String, Object>, Void> {

    public void
        readProductData(String file,
                        Consumer<String, Map<String, Object>> rowParser) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                Map<String, Object> properties = rowParser.consume(line);
                consume(properties);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
