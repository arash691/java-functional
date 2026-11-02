package shopflow.w09;

/** EX-09-03: run an action with {@code requestId} in the MDC, always cleaning up afterwards. */
public final class RequestScope {

    private RequestScope() {}

    public static void run(String requestId, Runnable action) {
        action.run();
    }
}
