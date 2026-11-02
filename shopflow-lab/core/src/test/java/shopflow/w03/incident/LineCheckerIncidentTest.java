package shopflow.w03.incident;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w03")
@DisplayName("INC-03-01 Carts flagged as different (RL-01)")
class LineCheckerIncidentTest {

    private static CartLine line(int quantity) {
        return new CartLine("ABC-0001", quantity);
    }

    @Test
    @DisplayName("GUARD: small equal quantities are the same")
    void smallEqualQuantitiesAreTheSame() {
        assertThat(LineChecker.sameQuantity(line(5), line(5))).isTrue();
        assertThat(LineChecker.sameQuantity(line(127), line(127))).isTrue();
    }

    @Test
    @DisplayName("GUARD: different quantities are different")
    void differentQuantitiesAreDifferent() {
        assertThat(LineChecker.sameQuantity(line(5), line(6))).isFalse();
        assertThat(LineChecker.sameQuantity(line(500), line(501))).isFalse();
    }

    @Test
    @DisplayName("REPRO: 128 and 128 are the same quantity")
    void quantity128() {
        assertThat(LineChecker.sameQuantity(line(128), line(128)))
                .as("two lines with quantity 128")
                .isTrue();
    }

    @Test
    @DisplayName("REPRO: 1000 and 1000 are the same quantity")
    void quantity1000() {
        assertThat(LineChecker.sameQuantity(line(1000), line(1000)))
                .as("two lines with quantity 1000")
                .isTrue();
    }

    @Test
    @DisplayName("GUARD: bulk threshold")
    void bulkThreshold() {
        assertThat(LineChecker.isBulk(line(99))).isFalse();
        assertThat(LineChecker.isBulk(line(100))).isTrue();
    }
}
