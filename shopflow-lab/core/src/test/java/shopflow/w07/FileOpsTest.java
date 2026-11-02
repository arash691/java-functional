package shopflow.w07;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("w07")
@DisplayName("EX-07-03 NIO file I/O")
class FileOpsTest {

    @TempDir
    Path dir;

    @Test
    void writesAndReadsLinesRoundTrip() throws IOException {
        Path file = dir.resolve("a.txt");
        FileOps.writeLines(file, List.of("one", "two", "three"));
        assertThat(FileOps.readLines(file)).containsExactly("one", "two", "three");
    }

    @Test
    @DisplayName("keeps non-ASCII text intact (UTF-8)")
    void utf8RoundTrip() throws IOException {
        Path file = dir.resolve("utf8.txt");
        FileOps.writeLines(file, List.of("Crème brûlée", "日本語"));
        assertThat(Files.readString(file, StandardCharsets.UTF_8)).contains("Crème brûlée").contains("日本語");
        assertThat(FileOps.readLines(file)).containsExactly("Crème brûlée", "日本語");
    }

    @Test
    void createsMissingParentDirectories() throws IOException {
        Path file = dir.resolve("x/y/z.txt");
        FileOps.writeLines(file, List.of("hi"));
        assertThat(file).exists();
    }

    @Test
    void replacesAnExistingFile() throws IOException {
        Path file = dir.resolve("a.txt");
        FileOps.writeLines(file, List.of("old", "old"));
        FileOps.writeLines(file, List.of("new"));
        assertThat(FileOps.readLines(file)).containsExactly("new");
    }

    @Test
    void readingAMissingFileSurfacesNoSuchFileException() {
        assertThatThrownBy(() -> FileOps.readLines(dir.resolve("missing.txt")))
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    void countsLinesContainingText() throws IOException {
        Path file = dir.resolve("log.txt");
        FileOps.writeLines(file, List.of("ERROR disk", "INFO ok", "ERROR net", "error lower"));
        assertThat(FileOps.countLinesContaining(file, "ERROR")).isEqualTo(2);
    }

    @Test
    void copiesAndOverwrites() throws IOException {
        Path from = dir.resolve("from.txt");
        Path to = dir.resolve("to.txt");
        FileOps.writeLines(from, List.of("Crème"));
        FileOps.writeLines(to, List.of("will be replaced"));
        FileOps.copyUtf8(from, to);
        assertThat(FileOps.readLines(to)).containsExactly("Crème");
    }
}
