package shopflow.api.w10;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import shopflow.api.w10.config.ShopProperties;
import shopflow.api.w10.shipping.ExpressShipping;
import shopflow.api.w10.shipping.ShippingPolicy;
import shopflow.api.w10.shipping.StandardShipping;

/**
 * EX-10-03: profiles. This test builds a tiny context that only scans the shipping package,
 * so a mistake here does not break the other tests.
 */
@Tag("w10")
@DisplayName("EX-10-03 shipping policy depends on the active profile")
class Ex3ShippingProfilesTest {

    @TestConfiguration
    @ComponentScan(basePackageClasses = ShippingPolicy.class)
    @EnableConfigurationProperties(ShopProperties.class)
    static class ShippingOnly {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(ShippingOnly.class)
            .withPropertyValues(
                    "shopflow.shop.name=T", "shopflow.shop.currency=EUR", "shopflow.shop.free-shipping-threshold=50.00");

    @Test
    @DisplayName("default profile: exactly one ShippingPolicy, the standard one")
    void standardByDefault() {
        runner.run(ctx -> {
            assertThat(ctx).hasNotFailed();
            assertThat(ctx).hasSingleBean(ShippingPolicy.class);
            assertThat(ctx.getBean(ShippingPolicy.class)).isInstanceOf(StandardShipping.class);
        });
    }

    @Test
    @DisplayName("profile 'express': exactly one ShippingPolicy, the express one, 15.00")
    void expressProfile() {
        runner.withPropertyValues("spring.profiles.active=express").run(ctx -> {
            assertThat(ctx).hasNotFailed();
            assertThat(ctx).hasSingleBean(ShippingPolicy.class);
            ShippingPolicy policy = ctx.getBean(ShippingPolicy.class);
            assertThat(policy).isInstanceOf(ExpressShipping.class);
            assertThat(policy.costFor(new BigDecimal("10.00"))).isEqualByComparingTo("15.00");
        });
    }

    @Test
    @DisplayName("standard shipping is 5.00 below the free-shipping threshold and 0.00 from it")
    void freeShippingThreshold() {
        runner.run(ctx -> {
            assertThat(ctx).hasNotFailed();
            assertThat(ctx).hasSingleBean(ShippingPolicy.class);
            ShippingPolicy policy = ctx.getBean(ShippingPolicy.class);
            assertThat(policy.costFor(new BigDecimal("49.99"))).isEqualByComparingTo("5.00");
            assertThat(policy.costFor(new BigDecimal("50.00"))).isEqualByComparingTo("0.00");
        });
    }
}
