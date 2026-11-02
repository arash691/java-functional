package shopflow.api.w10.discount;

import java.math.BigDecimal;

/** Applies discounts to a cart subtotal. Depends on DiscountService only (no cycle). */
public class CartPricing {

    private final DiscountService discounts;

    public CartPricing(DiscountService discounts) {
        this.discounts = discounts;
    }

    public BigDecimal total(BigDecimal subtotal) {
        return subtotal.subtract(discounts.discountFor(subtotal));
    }
}
