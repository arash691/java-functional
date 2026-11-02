package shopflow.w00;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w00")
class HelloTest {

    @Test
    void greetsByName() {
        assertThat(Hello.greet("Sara")).isEqualTo("Hello, Sara!");
    }
}
