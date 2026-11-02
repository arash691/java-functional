package shopflow.w04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("w04")
@DisplayName("SF-02 Money")
class MoneyTest {

    @Nested
    @DisplayName("creation")
    class Creation {
        @Test
        void keepsAmountAndCurrency() {
            Money m = Money.of("19.99", "EUR");
            assertThat(m.amount()).isEqualByComparingTo("19.99");
            assertThat(m.currency()).isEqualTo("EUR");
        }

        @Test
        void alwaysHasTwoDecimals() {
            assertThat(Money.of("5", "EUR").amount().toPlainString()).isEqualTo("5.00");
            assertThat(Money.of("1.5", "EUR").amount().toPlainString()).isEqualTo("1.50");
        }

        @ParameterizedTest(name = "{0} rounds HALF_EVEN to {1}")
        @CsvSource({"0.005, 0.00", "0.015, 0.02", "0.025, 0.02", "2.675, 2.68", "2.665, 2.66"})
        void roundsHalfEven(String input, String expected) {
            assertThat(Money.of(input, "EUR").amount().toPlainString()).isEqualTo(expected);
        }

        @ParameterizedTest
        @ValueSource(strings = {"eur", "EU", "EURO", "E1R", ""})
        void rejectsBadCurrencyCodes(String code) {
            assertThatThrownBy(() -> Money.of("1.00", code)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsNullCurrencyAndBadAmounts() {
            assertThatThrownBy(() -> Money.of("1.00", null)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Money.of("abc", "EUR")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Money.of(null, "EUR")).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void zeroHelper() {
            assertThat(Money.zero("USD").amount().toPlainString()).isEqualTo("0.00");
            assertThat(Money.zero("USD").currency()).isEqualTo("USD");
        }
    }

    @Nested
    @DisplayName("arithmetic")
    class Arithmetic {
        @Test
        void addsSameCurrency() {
            assertThat(Money.of("1.10", "EUR").add(Money.of("2.25", "EUR"))).isEqualTo(Money.of("3.35", "EUR"));
        }

        @Test
        void addingDifferentCurrenciesIsRejected_messageNamesBoth() {
            assertThatThrownBy(() -> Money.of("1.00", "EUR").add(Money.of("1.00", "USD")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EUR")
                    .hasMessageContaining("USD");
        }

        @Test
        void subtractCanGoNegative() {
            Money result = Money.of("1.00", "EUR").subtract(Money.of("2.50", "EUR"));
            assertThat(result).isEqualTo(Money.of("-1.50", "EUR"));
            assertThat(result.isNegative()).isTrue();
            assertThat(result.isPositive()).isFalse();
        }

        @Test
        void multiplyByAnInt() {
            assertThat(Money.of("19.99", "EUR").multiply(3)).isEqualTo(Money.of("59.97", "EUR"));
        }

        @Test
        void operationsDoNotChangeTheOperands() {
            Money a = Money.of("1.00", "EUR");
            a.add(Money.of("1.00", "EUR"));
            a.multiply(5);
            assertThat(a).isEqualTo(Money.of("1.00", "EUR"));
        }
    }

    @Nested
    @DisplayName("equality and printing")
    class EqualityAndPrinting {
        @Test
        void sameValueDifferentWrittenFormIsEqual() {
            assertThat(Money.of("1.5", "EUR")).isEqualTo(Money.of("1.50", "EUR"));
        }

        @Test
        void differentCurrencyIsNotEqual() {
            assertThat(Money.of("1.00", "EUR")).isNotEqualTo(Money.of("1.00", "USD"));
        }

        @Test
        void differentAmountIsNotEqual() {
            assertThat(Money.of("1.00", "EUR")).isNotEqualTo(Money.of("1.01", "EUR"));
        }

        @Test
        void toStringIsAmountThenCurrency() {
            assertThat(Money.of("19.99", "EUR").toString()).isEqualTo("19.99 EUR");
        }
    }
}
