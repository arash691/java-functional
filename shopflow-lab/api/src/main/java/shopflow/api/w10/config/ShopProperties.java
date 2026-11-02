package shopflow.api.w10.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from the {@code shopflow.shop.*} keys in shopflow-w10.yml. */
@ConfigurationProperties("shopflow.shop")
public record ShopProperties(String name, String currency, BigDecimal freeShippingThreshold) {
}
