package recng.recommendations.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Base class used to read data from file and save it in some kind of backend
 * storage.
 *
 * @author jon
 *
 */
public abstract class AbstractDataUploader implements DataReader {

    @Override
    public void readFile(String file) throws IOException {
        BufferedReader br = null;
        int i = 0;
        boolean successful = false;
        long start = System.currentTimeMillis();
        try {
            startUpload();
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                try {
                    Map<String, Object> properties = parse(line);
                    save(properties);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                i++;
                if (i % 10000 == 0) {
                    long dt = System.currentTimeMillis() - start;
                    long ups = 1000l * i / dt;
                    System.out.println("Uploaded " + i + " lines so far (" +
                        ups + " lps)..");
                }
            }
            successful = true;
        } finally {
            endUpload(successful);
            if (br != null)
                br.close();
        }
        System.out.println("Done uploading " + i + " lines.");
    }

    /**
     * Signals that the upload is about to begin.
     */
    protected abstract void startUpload();

    /**
     * Signals that the upload is completed.
     *
     * @param successful
     *            True if no errors occurred, false otherwise.
     */
    protected abstract void endUpload(boolean successful);

    /**
     * Parses a line in the file into a property map.
     */
    protected abstract Map<String, Object> parse(String line);

    /**
     * Saves a property map.
     */
    protected abstract void save(Map<String, Object> properties);
}
