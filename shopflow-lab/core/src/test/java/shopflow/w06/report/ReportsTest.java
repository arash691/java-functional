package shopflow.w06.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w06")
@DisplayName("INC-06-2 The 'optimised' report got slower")
class ReportsTest {

    @Test
    @DisplayName("GUARD: totals are correct")
    void sumIsCorrect() {
        List<Integer> amounts = Reports.newAmountsList();
        for (int i = 1; i <= 100; i++) {
            amounts.add(i);
        }
        assertThat(Reports.sumAll(amounts)).isEqualTo(5050);
    }

    @Test
    @DisplayName("summing 60,000 amounts takes less than 150 ms")
    void sumIsFast() {
        List<Integer> amounts = Reports.newAmountsList();
        for (int i = 0; i < 60_000; i++) {
            amounts.add(i);
        }

        long start = System.nanoTime();
        long total = Reports.sumAll(amounts);
        long millis = (System.nanoTime() - start) / 1_000_000;

        assertThat(total).isEqualTo(59_999L * 60_000L / 2);
        assertThat(millis).as("sumAll took %d ms", millis).isLessThan(150);
    }
}
