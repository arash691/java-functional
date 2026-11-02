package shopflow.api.w12.cart;

public class UnknownProductException extends RuntimeException {

    public UnknownProductException(String sku) {
        super("Unknown product: " + sku);
    }
}
