package recng.common.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * Util class for closing streams etc.
 * 
 * @author jon
 * 
 */
public class Closer {

    public static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable == null)
                continue;
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
