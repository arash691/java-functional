package shopflow.w09.debug;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * DBG-09: find the bug using ONLY the debugger (no print statements, no extra logging).
 * Technique: put a conditional breakpoint inside the loop in BulkPricer.totalCents() with the condition
 * {@code BulkPricer.lineCents(i) != ReferencePricer.lineCents(i)} and inspect the first line that stops.
 */
@Tag("w09")
class BulkPricerTest {

    @Test
    @DisplayName("guard: a small order is priced without discount")
    void smallOrder() {
        // index 1: unit 107, quantity 38
        assertThat(BulkPricer.lineCents(1)).isEqualTo(107L * 38);
    }

    @Test
    @DisplayName("the grand total matches the finance team's number")
    void grandTotalMatchesReference() {
        assertThat(BulkPricer.totalCents()).isEqualTo(ReferencePricer.totalCents());
    }
}
