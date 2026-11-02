package shopflow.api.w10.tax;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

/** EX-10-01: DE = 0.19, FR = 0.20, anything else = 0. */
@Component
public class FlatTaxPolicy implements TaxPolicy {

    @Override
    public BigDecimal rateFor(String countryCode) {
        return BigDecimal.ZERO; // TODO EX-10-01
    }
}
