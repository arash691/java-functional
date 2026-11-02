package shopflow.api.w12.ordering;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException(String customerId) {
        super("Cart of " + customerId + " is empty");
    }
}
