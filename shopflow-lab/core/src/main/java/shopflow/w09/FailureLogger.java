package shopflow.w09;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** EX-09-04: log a failure so the stack trace is kept. */
public class FailureLogger {

    private static final Logger log = LoggerFactory.getLogger(FailureLogger.class);

    /** ERROR level; the message mentions the order id; the exception itself is attached to the log event. */
    public void logFailure(String orderId, Exception e) {
        log.error("Order failed: " + orderId + " (" + e.getMessage() + ")");
    }
}
