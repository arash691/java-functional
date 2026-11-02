package shopflow.api.w10;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import shopflow.api.w10.config.ShopProperties;

/** SF-06: bad configuration must stop the application at startup, not fail later at runtime. */
@Tag("w10")
@DisplayName("SF-06 ShopProperties are validated at startup")
class Sf06ShopPropertiesValidationTest {

    @TestConfiguration
    @EnableConfigurationProperties(ShopProperties.class)
    static class Props {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(Props.class);

    @Test
    @DisplayName("valid configuration starts")
    void valid() {
        runner.withPropertyValues("shopflow.shop.name=ShopFlow", "shopflow.shop.currency=EUR",
                "shopflow.shop.free-shipping-threshold=50.00").run(ctx -> assertThat(ctx).hasNotFailed());
    }

    @Test
    @DisplayName("currency must be three capital letters")
    void badCurrency() {
        runner.withPropertyValues("shopflow.shop.name=ShopFlow", "shopflow.shop.currency=eur",
                "shopflow.shop.free-shipping-threshold=50.00").run(ctx -> assertThat(ctx).hasFailed());
    }

    @Test
    @DisplayName("name must not be blank")
    void blankName() {
        runner.withPropertyValues("shopflow.shop.name=", "shopflow.shop.currency=EUR",
                "shopflow.shop.free-shipping-threshold=50.00").run(ctx -> assertThat(ctx).hasFailed());
    }

    @Test
    @DisplayName("free-shipping threshold must not be negative")
    void negativeThreshold() {
        runner.withPropertyValues("shopflow.shop.name=ShopFlow", "shopflow.shop.currency=EUR",
                "shopflow.shop.free-shipping-threshold=-1").run(ctx -> assertThat(ctx).hasFailed());
    }
}
