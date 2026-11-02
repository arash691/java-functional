package shopflow.w09;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailureLogger {

    private static final Logger log = LoggerFactory.getLogger(FailureLogger.class);

    public void logFailure(String orderId, Exception e) {
        log.error("Order failed: {}", orderId, e);
    }
}
