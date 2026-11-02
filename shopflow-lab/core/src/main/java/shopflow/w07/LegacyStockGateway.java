package shopflow.w07;

/** The old warehouse API. Returns -1 for an unknown SKU and throws GatewayException when the system is down. */
@FunctionalInterface
public interface LegacyStockGateway {
    int available(String sku) throws GatewayException;
}
