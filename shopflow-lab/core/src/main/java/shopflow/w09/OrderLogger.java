package shopflow.w09;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** EX-09-01: log a business event with a parameterised message. */
public class OrderLogger {

    private static final Logger log = LoggerFactory.getLogger(OrderLogger.class);

    /** Must log at INFO with the message template {@code Order placed: id={} cents={}} and two arguments. */
    public void logPlaced(String orderId, long cents) {
        // TODO EX-09-01
    }
}
