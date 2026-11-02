package shopflow.w09.incident;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Logs every incoming request so support can trace a customer's problem. */
public class RequestLogger {

    private static final Logger log = LoggerFactory.getLogger(RequestLogger.class);

    public void logIncoming(HttpRequest request) {
        log.info("Incoming request: {}", request);
    }
}
