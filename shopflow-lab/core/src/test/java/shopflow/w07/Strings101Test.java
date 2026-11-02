package shopflow.w07;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w07")
@DisplayName("EX-07-04 Strings")
class Strings101Test {

    @Test
    void joinsSkusWithCommas() {
        assertThat(Strings101.joinSkus(List.of("A", "B", "C"))).isEqualTo("A,B,C");
        assertThat(Strings101.joinSkus(List.of("A"))).isEqualTo("A");
        assertThat(Strings101.joinSkus(List.of())).isEmpty();
    }

    @Test
    void masksAllButTheLastFourDigits() {
        assertThat(Strings101.maskCardNumber("4111111111111111")).isEqualTo("************1111");
        assertThat(Strings101.maskCardNumber("1234")).isEqualTo("1234");
    }

    @Test
    void maskRejectsNullAndShortInput() {
        assertThatThrownBy(() -> Strings101.maskCardNumber(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Strings101.maskCardNumber("123")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void slugifyBasics() {
        assertThat(Strings101.slugify("Blue Mug (Large)!")).isEqualTo("blue-mug-large");
        assertThat(Strings101.slugify("  --Hello   World--  ")).isEqualTo("hello-world");
        assertThat(Strings101.slugify("Tee 2026")).isEqualTo("tee-2026");
    }
}
