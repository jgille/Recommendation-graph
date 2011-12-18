package rectest.recommendations;

import java.util.Map;
import rectest.common.Consumer;

public interface PMDReader {

    void readMetadata(Consumer<String, Map<String, Object>> rowParser);
}
