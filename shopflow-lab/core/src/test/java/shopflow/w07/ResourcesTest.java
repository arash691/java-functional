package shopflow.w07;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w07")
@DisplayName("EX-07-02 try-with-resources")
class ResourcesTest {

    private final List<String> log = new ArrayList<>();

    @Nested
    class OneResource {
        @Test
        void closedAfterNormalUse() {
            TrackingResource r = new TrackingResource("a", log);
            Resources.useResource(() -> r, res -> log.add("use:" + res.name()));
            assertThat(r.closeCount()).isEqualTo(1);
            assertThat(log).containsExactly("use:a", "close:a");
        }

        @Test
        void closedEvenWhenTheActionThrows() {
            TrackingResource r = new TrackingResource("a", log);
            IllegalArgumentException boom = new IllegalArgumentException("boom");
            assertThatThrownBy(() -> Resources.useResource(() -> r, res -> {
                        throw boom;
                    }))
                    .isSameAs(boom);
            assertThat(r.closeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("when the action AND close both fail, the action's exception wins and the close failure is suppressed")
        void closeFailureIsSuppressed() {
            TrackingResource r = new TrackingResource("a", log, true);
            IllegalArgumentException boom = new IllegalArgumentException("boom");
            assertThatThrownBy(() -> Resources.useResource(() -> r, res -> {
                        throw boom;
                    }))
                    .isSameAs(boom)
                    .satisfies(t -> assertThat(t.getSuppressed()).hasSize(1));
            assertThat(boom.getSuppressed()[0]).hasMessage("close failed: a");
        }

        @Test
        void aCloseFailureAloneIsReported() {
            TrackingResource r = new TrackingResource("a", log, true);
            assertThatThrownBy(() -> Resources.useResource(() -> r, res -> { }))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("close failed: a");
        }
    }

    @Nested
    class TwoResources {
        @Test
        void closedInReverseOrderOfOpening() {
            Resources.useTwo(() -> new TrackingResource("a", log), () -> new TrackingResource("b", log), (a, b) -> { });
            assertThat(log).containsExactly("close:b", "close:a");
        }

        @Test
        void firstIsClosedWhenOpeningTheSecondFails() {
            TrackingResource a = new TrackingResource("a", log);
            assertThatThrownBy(() -> Resources.useTwo(
                            () -> a,
                            () -> {
                                throw new IllegalStateException("cannot open b");
                            },
                            (x, y) -> { }))
                    .hasMessage("cannot open b");
            assertThat(a.closeCount()).isEqualTo(1);
        }
    }
}
