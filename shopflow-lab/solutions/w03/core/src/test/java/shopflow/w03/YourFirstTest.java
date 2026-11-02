package shopflow.w03;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w03")
class YourFirstTest {

    @Test
    void shoutUpperCasesAndAddsExclamation() {
        assertThat(Greeter.shout("sara")).isEqualTo("SARA!");
    }

    @Test
    void emptyNameStillGetsAnExclamationMark() {
        assertThat(Greeter.shout("")).isEqualTo("!");
    }
}
