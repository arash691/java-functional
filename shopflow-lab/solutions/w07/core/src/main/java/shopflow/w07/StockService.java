package shopflow.w07;

public class StockService {
    private final LegacyStockGateway gateway;

    public StockService(LegacyStockGateway gateway) {
        this.gateway = gateway;
    }

    public int available(String sku) {
        int quantity;
        try {
            quantity = gateway.available(sku);
        } catch (GatewayException e) {
            throw new InventoryException("Could not read stock for " + sku, e);
        }
        if (quantity < 0) {
            throw new UnknownSkuException(sku);
        }
        return quantity;
    }

    public void require(String sku, int quantity) {
        int available = available(sku);
        if (available < quantity) {
            throw new OutOfStockException(sku, quantity, available);
        }
    }
}
