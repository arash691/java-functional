package shopflow.api.w10.shipping;

import java.math.BigDecimal;

public interface ShippingPolicy {

    BigDecimal costFor(BigDecimal subtotal);
}
