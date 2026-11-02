package shopflow.w03;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag("w03")
@DisplayName("EX-03-03 Shipping")
class ShippingTest {

    @ParameterizedTest(name = "{0}, {1} g -> {2} cents")
    @CsvSource({
        "EU,   500,  500",
        "EU,  1000,  500",
        "EU,  1001,  900",
        "EU,  5000,  900",
        "EU,  5001, 1700",
        "US,   100, 1200",
        "US,  3000, 1600",
        "ASIA, 9000, 2700",
    })
    void computesCostByZoneAndWeight(String zone, int grams, int expected) {
        assertThat(Shipping.costCents(zone, grams)).isEqualTo(expected);
    }

    @Test
    void unknownZoneIsRejectedWithTheZoneInTheMessage() {
        assertThatThrownBy(() -> Shipping.costCents("MARS", 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MARS");
    }

    @Test
    void nonPositiveWeightIsRejected() {
        assertThatThrownBy(() -> Shipping.costCents("EU", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
