package shopflow.w06;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import shopflow.w06.example.CollectionsTour;

@Tag("w06")
@DisplayName("Worked example (green): the collections tour")
class WorkedExampleTest {

    @Test
    void setKeepsFirstSeenOrder() {
        assertThat(CollectionsTour.dedupe(List.of("b", "a", "b", "c"))).containsExactly("b", "a", "c");
    }

    @Test
    void mapCountsOccurrences() {
        assertThat(CollectionsTour.countLetters(List.of("b", "a", "b", "c")))
                .containsEntry("b", 2).containsEntry("a", 1).containsEntry("c", 1);
    }
}
