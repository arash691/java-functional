package shopflow.api.w10.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import shopflow.api.w10.config.ShopProperties;

@Component("shop")
public class ShopHealthIndicator implements HealthIndicator {

    private final ShopProperties shop;

    public ShopHealthIndicator(ShopProperties shop) {
        this.shop = shop;
    }

    @Override
    public Health health() {
        return Health.up().withDetail("name", shop.name()).build();
    }
}
