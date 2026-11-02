package shopflow.api.w10.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import shopflow.api.w10.config.ShopProperties;

/** EX-10-04: report UP with a detail "shop" = the shop name. Appears in /actuator/health as component "shop". */
public class ShopHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.down().build(); // TODO EX-10-04
    }
}
