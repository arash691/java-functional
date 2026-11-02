package shopflow.w07;

import java.nio.file.Path;
import java.util.Map;

/**
 * SF-04: persist the stock map as JSON: {"version":1,"stock":{"SKU-1":5,"SKU-2":0}}.
 * Use Jackson (already on the classpath). Rules are in InventoryStoreTest.
 */
public class InventoryStore {

    /** Writes the file (UTF-8, keys sorted), creating missing parent directories. IO problems -> StoreException with cause. */
    public void save(Map<String, Integer> stock, Path file) {
        throw new UnsupportedOperationException("TODO SF-04");
    }

    /**
     * Reads the file. Problems become StoreException (never raw IOException or Jackson exceptions):
     * missing file -> message contains "not found" and the path; invalid/empty JSON, missing "stock",
     * or a negative quantity -> message contains "corrupt" and the path; version other than 1 -> "unsupported version".
     */
    public Map<String, Integer> load(Path file) {
        throw new UnsupportedOperationException("TODO SF-04");
    }
}
