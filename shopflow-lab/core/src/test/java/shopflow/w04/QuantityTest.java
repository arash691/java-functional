package shopflow.w04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w04")
@DisplayName("EX-04-02 Quantity")
class QuantityTest {

    @Test
    void ofKeepsTheValue() {
        assertThat(Quantity.of(7).value()).isEqualTo(7);
    }

    @Test
    void zeroConstantHasValueZero() {
        assertThat(Quantity.ZERO.value()).isZero();
    }

    @Test
    void negativeValuesAreRejected() {
        assertThatThrownBy(() -> Quantity.of(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void plusAddsAndReturnsANewQuantity() {
        Quantity two = Quantity.of(2);
        Quantity three = Quantity.of(3);
        Quantity five = two.plus(three);
        assertThat(five.value()).isEqualTo(5);
        assertThat(two.value()).as("operands are unchanged").isEqualTo(2);
        assertThat(three.value()).isEqualTo(3);
    }
}
