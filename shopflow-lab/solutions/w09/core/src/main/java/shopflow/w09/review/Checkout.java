package shopflow.w09.review;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Checkout {

    public record Receipt(String orderId, BigDecimal total, Instant issuedAt) {}

    private final Clock clock;

    public Checkout(Clock clock) {
        this.clock = clock;
    }

    /** Sum of unit price times quantity for every line. */
    public BigDecimal total(Cart cart, Map<Sku, BigDecimal> priceList) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Cart.Line line : cart.lines()) {
            sum = sum.add(priceList.get(line.sku()).multiply(BigDecimal.valueOf(line.quantity())));
        }
        return sum;
    }

    public Receipt receipt(String orderId, Cart cart, Map<Sku, BigDecimal> priceList) {
        return new Receipt(orderId, total(cart, priceList), clock.instant());
    }

    /** Reads lines like {@code SKU-1,0.10}. */
    public Map<Sku, BigDecimal> loadPriceList(Path file) {
        Map<Sku, BigDecimal> prices = new HashMap<>();
        try {
            for (String line : Files.readAllLines(file)) {
                String[] parts = line.split(",");
                prices.put(new Sku(parts[0].trim()), new BigDecimal(parts[1].trim()));
            }
        } catch (IOException | RuntimeException e) {
            throw new PriceListException("Cannot read price list " + file, e);
        }
        return prices;
    }
}
