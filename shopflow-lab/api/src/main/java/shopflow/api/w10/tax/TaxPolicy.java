package shopflow.api.w10.tax;

import java.math.BigDecimal;

/** Knows the tax rate (as a fraction, e.g. 0.19) for a country. */
public interface TaxPolicy {

    BigDecimal rateFor(String countryCode);
}
