package shopflow.w07;

/**
 * EX-07-01: complete this exception. Message format:
 * "Out of stock: SKU-1 (requested 5, available 2)". Expose sku(), requested(), available().
 */
public class OutOfStockException extends InventoryException {

    public OutOfStockException(String sku, int requested, int available) {
        super("TODO");
    }

    public String sku() {
        return null;
    }

    public int requested() {
        return 0;
    }

    public int available() {
        return 0;
    }
}
