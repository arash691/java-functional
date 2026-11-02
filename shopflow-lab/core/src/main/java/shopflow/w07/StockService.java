package shopflow.w07;

/**
 * EX-07-01: exception translation. Callers of this service must not need to know about GatewayException.
 */
public class StockService {
    private final LegacyStockGateway gateway;

    public StockService(LegacyStockGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * Available quantity. Gateway returns -1 -> UnknownSkuException.
     * GatewayException -> InventoryException("Could not read stock for SKU-1") WITH the original as cause.
     */
    public int available(String sku) {
        throw new UnsupportedOperationException("TODO EX-07-01");
    }

    /** Throws OutOfStockException when fewer than {@code quantity} units are available. */
    public void require(String sku, int quantity) {
        throw new UnsupportedOperationException("TODO EX-07-01");
    }
}
