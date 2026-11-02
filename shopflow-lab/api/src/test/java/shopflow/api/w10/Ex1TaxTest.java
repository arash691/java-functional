package shopflow.api.w10;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import shopflow.api.w10.tax.TaxCalculator;
import shopflow.api.w10.tax.TaxPolicy;

/** EX-10-01: beans and constructor injection. */
@Tag("w10")
@SpringBootTest
@DisplayName("EX-10-01 TaxCalculator is wired with a TaxPolicy")
class Ex1TaxTest {

    @Autowired
    TaxPolicy policy;

    @Autowired
    TaxCalculator calculator;

    @Test
    @DisplayName("the policy knows DE = 0.19 and FR = 0.20")
    void knownRates() {
        assertThat(policy.rateFor("DE")).as("DE").isEqualByComparingTo("0.19");
        assertThat(policy.rateFor("FR")).as("FR").isEqualByComparingTo("0.20");
    }

    @Test
    @DisplayName("other countries are untaxed")
    void unknownCountry() {
        assertThat(policy.rateFor("US")).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("the calculator taxes the net amount using the policy")
    void taxesNet() {
        assertThat(calculator.taxFor(new BigDecimal("100.00"), "DE")).isEqualTo(new BigDecimal("19.00"));
        assertThat(calculator.taxFor(new BigDecimal("100.00"), "US")).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("results are rounded HALF_UP to 2 decimals (19.99 * 0.19 = 3.7981 -> 3.80)")
    void rounding() {
        assertThat(calculator.taxFor(new BigDecimal("19.99"), "DE")).isEqualTo(new BigDecimal("3.80"));
    }

    @Test
    @DisplayName("TaxCalculator gets its TaxPolicy through the constructor (not a field, not new)")
    void constructorInjection() {
        var constructors = TaxCalculator.class.getConstructors();
        assertThat(constructors).hasSize(1);
        assertThat(constructors[0].getParameterTypes())
                .as("constructor parameters of TaxCalculator")
                .contains(TaxPolicy.class);
    }
}
