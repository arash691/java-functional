package shopflow.w07;

/** EX-07-01: message "Unknown SKU: X"; expose sku(). */
public class UnknownSkuException extends InventoryException {

    public UnknownSkuException(String sku) {
        super("TODO");
    }

    public String sku() {
        return null;
    }
}
