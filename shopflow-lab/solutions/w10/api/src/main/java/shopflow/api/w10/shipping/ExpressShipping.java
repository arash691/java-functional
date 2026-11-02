package shopflow.api.w10.shipping;

import java.math.BigDecimal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("express")
public class ExpressShipping implements ShippingPolicy {

    @Override
    public BigDecimal costFor(BigDecimal subtotal) {
        return new BigDecimal("15.00");
    }
}
