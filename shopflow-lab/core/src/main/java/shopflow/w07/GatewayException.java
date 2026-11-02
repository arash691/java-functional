package shopflow.w07;

/** Checked exception thrown by the legacy warehouse system. */
public class GatewayException extends Exception {
    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
