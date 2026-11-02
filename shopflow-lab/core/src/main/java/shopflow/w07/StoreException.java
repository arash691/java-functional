package shopflow.w07;

/** Thrown when the inventory store file cannot be read or written. Always carries the original cause when there is one. */
public class StoreException extends RuntimeException {
    public StoreException(String message) {
        super(message);
    }

    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
