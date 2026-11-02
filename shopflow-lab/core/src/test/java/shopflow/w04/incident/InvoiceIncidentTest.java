package shopflow.w04.incident;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag("w04")
@DisplayName("INC-04-02 Invoice off by one cent (RL-03)")
class InvoiceIncidentTest {

    @ParameterizedTest(name = "GUARD: {0} -> {1} cents")
    @CsvSource({"0.50, 50", "0.25, 25", "2.00, 200", "10.00, 1000"})
    void roundPricesAreFine(String price, long expectedCents) {
        Invoice invoice = new Invoice();
        invoice.addLine(new BigDecimal(price), 1);
        assertThat(invoice.totalCents()).isEqualTo(expectedCents);
    }

    @ParameterizedTest(name = "REPRO: one line of {0} must be charged as {1} cents")
    @CsvSource({"0.29, 29", "0.57, 57", "1.15, 115", "19.99, 1999"})
    void singleLine(String price, long expectedCents) {
        Invoice invoice = new Invoice();
        invoice.addLine(new BigDecimal(price), 1);
        assertThat(invoice.totalCents()).isEqualTo(expectedCents);
    }

    @Test
    @DisplayName("REPRO: ten lines of 0.10 must total exactly 1.00")
    void tenTimesTenCents() {
        Invoice invoice = new Invoice();
        for (int i = 0; i < 10; i++) {
            invoice.addLine(new BigDecimal("0.10"), 1);
        }
        assertThat(invoice.totalCents()).as("cents charged").isEqualTo(100);
        assertThat(invoice.total()).as("total").isEqualByComparingTo("1.00");
    }
}
