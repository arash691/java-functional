package shopflow.w09.incident;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestLogger {

    private static final Logger log = LoggerFactory.getLogger(RequestLogger.class);

    /** Only these headers are safe to log; everything else is dropped. The body is never logged. */
    private static final Set<String> SAFE_HEADERS = Set.of("x-request-id", "content-type", "accept");

    public void logIncoming(HttpRequest request) {
        StringBuilder safeHeaders = new StringBuilder();
        request.headers().forEach((name, value) -> {
            if (SAFE_HEADERS.contains(name.toLowerCase())) {
                safeHeaders.append(name).append('=').append(value).append(' ');
            }
        });
        log.info("Incoming request: {} {} headers=[{}] bodyLength={}",
                request.method(), request.path(), safeHeaders.toString().trim(), request.body() == null ? 0 : request.body().length());
    }
}
