package shopflow.w03;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w03")
@DisplayName("EX-03-01 Pricing")
class PricingTest {

    @Nested
    @DisplayName("lineTotalCents")
    class LineTotal {
        @Test
        void multipliesPriceByQuantity() {
            assertThat(Pricing.lineTotalCents(250, 4)).isEqualTo(1000);
        }

        @Test
        void zeroQuantityIsZero() {
            assertThat(Pricing.lineTotalCents(250, 0)).isZero();
        }

        @Test
        void negativeQuantityIsRejected() {
            assertThatThrownBy(() -> Pricing.lineTotalCents(250, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("applyDiscountPercent")
    class Discount {
        @Test
        void tenPercentOffRoundNumber() {
            assertThat(Pricing.applyDiscountPercent(1000, 10)).isEqualTo(900);
        }

        @Test
        void fractionOfACentIsDroppedFromTheDiscount() {
            // discount = 999 * 15 / 100 = 149 (integer division), so 999 - 149 = 850
            assertThat(Pricing.applyDiscountPercent(999, 15)).isEqualTo(850);
        }

        @Test
        void zeroAndHundredPercent() {
            assertThat(Pricing.applyDiscountPercent(500, 0)).isEqualTo(500);
            assertThat(Pricing.applyDiscountPercent(500, 100)).isZero();
        }

        @Test
        void percentOutOfRangeIsRejected() {
            assertThatThrownBy(() -> Pricing.applyDiscountPercent(500, 101))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Pricing.applyDiscountPercent(500, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("format (overloaded)")
    class Format {
        @Test
        void formatsEurosAndCents() {
            assertThat(Pricing.format(1999)).isEqualTo("19.99");
        }

        @Test
        void padsSmallAmounts() {
            assertThat(Pricing.format(5)).isEqualTo("0.05");
            assertThat(Pricing.format(0)).isEqualTo("0.00");
            assertThat(Pricing.format(100)).isEqualTo("1.00");
        }

        @Test
        void overloadWithCurrencyPrefixesCode() {
            assertThat(Pricing.format(1999, "EUR")).isEqualTo("EUR 19.99");
        }
    }
}
