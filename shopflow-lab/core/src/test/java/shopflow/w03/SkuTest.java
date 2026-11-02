package shopflow.w03;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("w03")
@DisplayName("EX-03-02 Sku")
class SkuTest {

    @Test
    void acceptsTheCanonicalShape() {
        assertThat(Sku.isValid("ABC-1234")).as("ABC-1234").isTrue();
        assertThat(Sku.isValid("ZZZ-0000")).as("ZZZ-0000").isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc-1234", "AB-12345", "ABC_1234", "ABC-12a4", "ABCD-123", "ABC-123", "ABC-12345", " ABC-1234"})
    void rejectsEverythingElse(String candidate) {
        assertThat(Sku.isValid(candidate)).as("isValid(%s)", candidate).isFalse();
    }

    @Test
    void prefixIsTheLetters() {
        assertThat(Sku.prefix("ABC-1234")).isEqualTo("ABC");
    }

    @Test
    void numberIsTheDigitsAsAnInt() {
        assertThat(Sku.number("ABC-0042")).isEqualTo(42);
        assertThat(Sku.number("ABC-1234")).isEqualTo(1234);
    }

    @Test
    void prefixAndNumberRejectInvalidSkus() {
        assertThatThrownBy(() -> Sku.prefix("nope")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sku.number(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
