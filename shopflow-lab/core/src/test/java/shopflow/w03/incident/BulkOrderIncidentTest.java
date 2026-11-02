package shopflow.w03.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w03")
@DisplayName("INC-03-02 Negative order total (RL-02)")
class BulkOrderIncidentTest {

    @Test
    @DisplayName("GUARD: normal totals still work")
    void normalTotals() {
        assertThat(BulkOrder.totalCents(250, 4)).isEqualTo(1000);
        assertThat(BulkOrder.sumCents(100, 200, 300)).isEqualTo(600);
    }

    @Test
    @DisplayName("GUARD: the largest exact int result is allowed")
    void largestExactResult() {
        assertThat(BulkOrder.totalCents(Integer.MAX_VALUE, 1)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("REPRO: 10,000 units at 2,500.00 must not wrap around to a negative total")
    void multiplicationOverflowIsDetected() {
        assertThatThrownBy(() -> BulkOrder.totalCents(250_000, 10_000))
                .as("2,500,000,000 cents does not fit into an int")
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    @DisplayName("REPRO: summing lines must not wrap around either")
    void sumOverflowIsDetected() {
        assertThatThrownBy(() -> BulkOrder.sumCents(Integer.MAX_VALUE, 1))
                .isInstanceOf(ArithmeticException.class);
    }
}
