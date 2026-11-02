package shopflow.w04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w04")
class RectangleTest {

    @Test
    void computesArea() {
        assertThat(new Rectangle(3, 4).area()).isEqualTo(12);
    }

    @Test
    void scalingReturnsANewObjectAndKeepsTheOriginal() {
        Rectangle original = new Rectangle(2, 3);
        Rectangle bigger = original.scaledBy(2);
        assertThat(bigger.area()).isEqualTo(24);
        assertThat(original.area()).as("original is unchanged").isEqualTo(6);
    }

    @Test
    void rejectsNonPositiveSides() {
        assertThatThrownBy(() -> new Rectangle(0, 3)).isInstanceOf(IllegalArgumentException.class);
    }
}
