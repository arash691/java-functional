package shopflow.w09;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** EX-09-02: pick the right level. */
public class StockAlerts {

    private static final Logger log = LoggerFactory.getLogger(StockAlerts.class);

    /**
     * Quantity below the threshold: WARN {@code Low stock: sku={} qty={} threshold={}}.
     * Otherwise: DEBUG {@code Stock ok: sku={} qty={}}.
     */
    public void check(String sku, int qty, int threshold) {
        log.info("checked {}", sku);
    }
}
