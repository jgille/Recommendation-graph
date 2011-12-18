package rectest.recommendations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import rectest.common.Consumer;

public abstract class AbstractPMDReader
    implements PMDReader, Consumer<Map<String, Object>, Void> {

    private final String fileName;

    public AbstractPMDReader(String fileName) {
        this.fileName = fileName;
    }

    public void readMetadata(Consumer<String, Map<String, Object>> rowParser) {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            String line = null;
            while ((line = br.readLine()) != null) {
                Map<String, Object> properties = rowParser.consume(line);
                consume(properties);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
