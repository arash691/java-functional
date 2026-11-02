package shopflow.w09;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderLogger {

    private static final Logger log = LoggerFactory.getLogger(OrderLogger.class);

    public void logPlaced(String orderId, long cents) {
        log.info("Order placed: id={} cents={}", orderId, cents);
    }
}
