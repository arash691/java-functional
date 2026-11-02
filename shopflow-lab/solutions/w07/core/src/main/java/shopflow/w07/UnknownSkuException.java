package shopflow.w07;

public class UnknownSkuException extends InventoryException {
    private final String sku;

    public UnknownSkuException(String sku) {
        super("Unknown SKU: " + sku);
        this.sku = sku;
    }

    public String sku() {
        return sku;
    }
}
