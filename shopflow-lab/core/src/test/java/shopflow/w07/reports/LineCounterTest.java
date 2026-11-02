package shopflow.w07.reports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w07")
@DisplayName("INC-07-2 File handles pile up on report failures")
class LineCounterTest {

    private static final Path ANY = Path.of("report.txt");

    @Test
    @DisplayName("GUARD: counts lines and closes the stream")
    void countsAndCloses() {
        TrackingInputStream stream = new TrackingInputStream("a\nb\nc\n");
        assertThat(new LineCounter(p -> stream).countLines(ANY)).isEqualTo(3);
        assertThat(stream.isClosed()).isTrue();
    }

    @Test
    @DisplayName("GUARD: an empty file has zero lines")
    void emptyFile() {
        assertThat(new LineCounter(p -> new TrackingInputStream("")).countLines(ANY)).isZero();
    }

    @Test
    @DisplayName("GUARD: failing to open the file is reported as UncheckedIOException")
    void openFails() {
        LineCounter counter = new LineCounter(p -> {
            throw new IOException("cannot open");
        });
        assertThatThrownBy(() -> counter.countLines(ANY))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("cannot open");
    }

    @Test
    @DisplayName("the stream is closed even when reading fails half-way")
    void closedWhenReadingFails() {
        TrackingInputStream stream = new TrackingInputStream("a\nb\nc\nd\n", 4);

        assertThatThrownBy(() -> new LineCounter(p -> stream).countLines(ANY))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("disk error");

        assertThat(stream.isClosed()).as("stream closed after the failure").isTrue();
    }
}
