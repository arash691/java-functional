package shopflow.w09.review;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
        double sum = 0;
        for (Cart.Line line : cart.lines()) {
            sum += priceList.get(line.sku()).doubleValue() * line.quantity();
        }
        return BigDecimal.valueOf(sum);
    }

    public Receipt receipt(String orderId, Cart cart, Map<Sku, BigDecimal> priceList) {
        Instant issuedAt = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        return new Receipt(orderId, total(cart, priceList), issuedAt);
    }

    /** Reads lines like {@code SKU-1,0.10}. */
    public Map<Sku, BigDecimal> loadPriceList(Path file) {
        Map<Sku, BigDecimal> prices = new HashMap<>();
        try {
            for (String line : Files.readAllLines(file)) {
                String[] parts = line.split(",");
                prices.put(new Sku(parts[0].trim()), new BigDecimal(parts[1].trim()));
            }
        } catch (Exception e) {
            // the price list is optional
        }
        return prices;
    }
}
