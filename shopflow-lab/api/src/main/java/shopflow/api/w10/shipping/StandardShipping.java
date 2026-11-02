package shopflow.api.w10.shipping;

import java.math.BigDecimal;

/** EX-10-03: the default policy. 5.00 flat; 0.00 when the subtotal reaches the free-shipping threshold (50.00). */
public class StandardShipping implements ShippingPolicy {

    @Override
    public BigDecimal costFor(BigDecimal subtotal) {
        return new BigDecimal("5.00");
    }
}
