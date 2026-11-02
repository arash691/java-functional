package shopflow.api.w10.tax;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class FlatTaxPolicy implements TaxPolicy {

    private static final Map<String, BigDecimal> RATES = Map.of(
            "DE", new BigDecimal("0.19"),
            "FR", new BigDecimal("0.20"));

    @Override
    public BigDecimal rateFor(String countryCode) {
        return RATES.getOrDefault(countryCode, BigDecimal.ZERO);
    }
}
