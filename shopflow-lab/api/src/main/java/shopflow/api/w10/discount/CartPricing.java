package shopflow.api.w10.discount;

import java.math.BigDecimal;

/** Applies discounts to a cart subtotal. */
public class CartPricing {

    private final DiscountService discounts;

    public CartPricing(DiscountService discounts) {
        this.discounts = discounts;
    }

    public BigDecimal total(BigDecimal subtotal) {
        return subtotal.subtract(discounts.discountFor(subtotal));
    }

    public boolean isLargeOrder(BigDecimal subtotal) {
        return subtotal.compareTo(new BigDecimal("100.00")) >= 0;
    }
}
