package shopflow.api.w10.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 10% off large orders. */
public class DiscountService {

    private final CartPricing pricing;

    public DiscountService(CartPricing pricing) {
        this.pricing = pricing;
    }

    public BigDecimal discountFor(BigDecimal subtotal) {
        if (pricing.isLargeOrder(subtotal)) {
            return subtotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2);
    }
}
