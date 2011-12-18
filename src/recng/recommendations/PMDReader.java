package recng.recommendations;

import java.util.Map;

import recng.common.Consumer;

public interface PMDReader {

    void readMetadata(Consumer<String, Map<String, Object>> rowParser);
}
