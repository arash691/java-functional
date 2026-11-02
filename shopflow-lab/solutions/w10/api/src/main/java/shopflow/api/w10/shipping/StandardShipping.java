package shopflow.api.w10.shipping;

import java.math.BigDecimal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import shopflow.api.w10.config.ShopProperties;

@Component
@Profile("!express")
public class StandardShipping implements ShippingPolicy {

    private final ShopProperties shop;

    public StandardShipping(ShopProperties shop) {
        this.shop = shop;
    }

    @Override
    public BigDecimal costFor(BigDecimal subtotal) {
        if (subtotal.compareTo(shop.freeShippingThreshold()) >= 0) {
            return new BigDecimal("0.00");
        }
        return new BigDecimal("5.00");
    }
}
