package shopflow.api.w10.shipping;

import java.math.BigDecimal;

/** EX-10-03: only when the "express" profile is active. 15.00 flat. */
public class ExpressShipping implements ShippingPolicy {

    @Override
    public BigDecimal costFor(BigDecimal subtotal) {
        return new BigDecimal("15.00");
    }
}
