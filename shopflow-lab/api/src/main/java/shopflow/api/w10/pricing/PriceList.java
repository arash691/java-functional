package shopflow.api.w10.pricing;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/** Fixed demo prices (in week 12 this comes from the database). */
@Component
public class PriceList {

    private static final Map<String, BigDecimal> PRICES = Map.of(
            "SKU-1", new BigDecimal("10.00"),
            "SKU-2", new BigDecimal("5.00"),
            "SKU-3", new BigDecimal("25.00"));

    public Optional<BigDecimal> priceOf(String sku) {
        return Optional.ofNullable(PRICES.get(sku));
    }
}
