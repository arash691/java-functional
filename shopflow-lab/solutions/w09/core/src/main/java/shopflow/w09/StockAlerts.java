package shopflow.w09;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockAlerts {

    private static final Logger log = LoggerFactory.getLogger(StockAlerts.class);

    public void check(String sku, int qty, int threshold) {
        if (qty < threshold) {
            log.warn("Low stock: sku={} qty={} threshold={}", sku, qty, threshold);
        } else {
            log.debug("Stock ok: sku={} qty={}", sku, qty);
        }
    }
}
