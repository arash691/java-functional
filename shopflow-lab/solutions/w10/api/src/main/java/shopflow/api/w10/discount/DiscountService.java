package shopflow.api.w10.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 10% off large orders. The large-order rule lives here, so there is no need to call back into CartPricing. */
public class DiscountService {

    private static final BigDecimal LARGE_ORDER = new BigDecimal("100.00");

    public BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(LARGE_ORDER) >= 0) {
            return subtotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2);
    }
}
