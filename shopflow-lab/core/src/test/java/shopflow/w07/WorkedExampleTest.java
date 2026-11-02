package shopflow.w07;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import shopflow.w07.example.InvalidQuantityException;
import shopflow.w07.example.QuantityParser;

@Tag("w07")
@DisplayName("Worked example (green): translating exceptions")
class WorkedExampleTest {

    @Test
    void parsesAndTrims() {
        assertThat(QuantityParser.parse(" 12 ")).isEqualTo(12);
    }

    @Test
    void keepsTheOriginalCause() {
        assertThatThrownBy(() -> QuantityParser.parse("twelve"))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessageContaining("twelve")
                .hasCauseInstanceOf(NumberFormatException.class);
    }

    @Test
    void rejectsNonPositive() {
        assertThatThrownBy(() -> QuantityParser.parse("0")).isInstanceOf(InvalidQuantityException.class);
    }
}
