package shopflow.api.w10.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

@Service
public class TaxCalculator {

    private final TaxPolicy policy;

    public TaxCalculator(TaxPolicy policy) {
        this.policy = policy;
    }

    public BigDecimal taxFor(BigDecimal net, String countryCode) {
        return net.multiply(policy.rateFor(countryCode)).setScale(2, RoundingMode.HALF_UP);
    }
}
