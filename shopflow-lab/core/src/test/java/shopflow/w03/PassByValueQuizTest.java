package shopflow.w03;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w03")
@DisplayName("EX-03-04 Pass by value: predict, then check")
class PassByValueQuizTest {

    @Test
    void yourPredictionsMatchTheJvm() {
        int[] actual = Scenarios.actual();
        int[] predicted = PassByValueQuiz.predictions();
        assertThat(predicted)
                .as("Edit PassByValueQuiz.predictions(); index 0=addOne, 1=setFirstToNine, 2=replaceArray, 3=shout")
                .containsExactly(actual);
    }
}
