package shopflow.w03;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w03")
class WorkedExampleTest {

    @Test
    void addsTwoNumbers() {
        assertThat(WorkedExample.add(2, 3)).isEqualTo(5);
    }

    @Test
    void classifiesStockLevels() {
        assertThat(WorkedExample.classify(0)).isEqualTo("OUT");
        assertThat(WorkedExample.classify(4)).isEqualTo("LOW");
        assertThat(WorkedExample.classify(5)).isEqualTo("OK");
    }
}
