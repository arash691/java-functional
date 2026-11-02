package shopflow.api.w10.tax;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

/** EX-10-01: net * rate, rounded to 2 decimals (HALF_UP). Get the rate from a TaxPolicy given to the constructor. */
@Service
public class TaxCalculator {

    public TaxCalculator() {
        // TODO EX-10-01: receive a TaxPolicy here (constructor injection)
    }

    public BigDecimal taxFor(BigDecimal net, String countryCode) {
        return BigDecimal.ZERO; // TODO EX-10-01
    }
}
