package shopflow.api.w10;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import shopflow.api.w10.discount.CartPricing;
import shopflow.api.w10.discount.DiscountService;

/** INC-34: "the app does not start after the discount feature was merged". */
@Tag("w10")
@DisplayName("INC-34 discount feature wires up without a circular dependency")
class Inc34CircularDependencyTest {

    // Same wiring Spring does for @Service classes, without touching the real application context.
    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withBean(CartPricing.class).withBean(DiscountService.class);

    @Test
    @DisplayName("the context starts (circular references are NOT allowed, as in a real Boot app)")
    void starts() {
        runner.run(ctx -> assertThat(ctx).hasNotFailed());
    }

    @Test
    @DisplayName("orders of 100.00 or more get 10% off")
    void discountForLargeOrders() {
        runner.run(ctx -> {
            assertThat(ctx).hasNotFailed();
            assertThat(ctx.getBean(CartPricing.class).total(new BigDecimal("200.00")))
                    .isEqualByComparingTo("180.00");
        });
    }

    @Test
    @DisplayName("GUARD: small orders pay full price")
    void smallOrders() {
        // Works only when the context starts; kept separate so the red/green story is clear.
        runner.run(ctx -> {
            if (ctx.getStartupFailure() == null) {
                assertThat(ctx.getBean(CartPricing.class).total(new BigDecimal("50.00")))
                        .isEqualByComparingTo("50.00");
            }
        });
    }
}
