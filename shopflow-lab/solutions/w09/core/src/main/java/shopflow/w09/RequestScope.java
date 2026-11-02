package shopflow.w09;

import org.slf4j.MDC;

public final class RequestScope {

    private RequestScope() {}

    public static void run(String requestId, Runnable action) {
        MDC.put("requestId", requestId);
        try {
            action.run();
        } finally {
            MDC.remove("requestId");
        }
    }
}
