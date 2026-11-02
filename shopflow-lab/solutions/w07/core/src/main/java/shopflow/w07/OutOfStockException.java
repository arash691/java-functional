package shopflow.w07;

public class OutOfStockException extends InventoryException {
    private final String sku;
    private final int requested;
    private final int available;

    public OutOfStockException(String sku, int requested, int available) {
        super("Out of stock: " + sku + " (requested " + requested + ", available " + available + ")");
        this.sku = sku;
        this.requested = requested;
        this.available = available;
    }

    public String sku() {
        return sku;
    }

    public int requested() {
        return requested;
    }

    public int available() {
        return available;
    }
}
