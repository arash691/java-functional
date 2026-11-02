package shopflow.w07.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("w07")
@DisplayName("INC-07-1 The import says success but nothing was saved")
class ImportServiceTest {

    @TempDir
    Path dir;

    private final ImportService service = new ImportService();

    @Test
    @DisplayName("GUARD: a good file is imported and written")
    void happyPath() throws IOException {
        Path csv = Files.write(dir.resolve("in.csv"), List.of("A,2", "B,3", "A,3"));
        Path target = dir.resolve("out.txt");

        ImportResult result = service.importCsv(csv, target);

        assertThat(result).isEqualTo(new ImportResult(true, 2, 0));
        assertThat(Files.readAllLines(target)).containsExactly("A=5", "B=3");
    }

    @Test
    @DisplayName("GUARD: bad lines are skipped and counted, not fatal")
    void badLinesAreSkipped() throws IOException {
        Path csv = Files.write(dir.resolve("in.csv"), List.of("A,2", "oops", "C,x", "B,1"));
        Path target = dir.resolve("out.txt");

        ImportResult result = service.importCsv(csv, target);

        assertThat(result).isEqualTo(new ImportResult(true, 2, 2));
        assertThat(Files.readAllLines(target)).containsExactly("A=2", "B=1");
    }

    @Test
    @DisplayName("an unwritable target fails loudly with the cause attached")
    void targetDirectoryMissing() throws IOException {
        Path csv = Files.write(dir.resolve("in.csv"), List.of("A,2"));
        Path target = dir.resolve("missing/dir/out.txt");

        assertThatThrownBy(() -> service.importCsv(csv, target))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining(target.toString())
                .hasCauseInstanceOf(NoSuchFileException.class);
    }

    @Test
    @DisplayName("a missing input file fails loudly with the cause attached")
    void inputFileMissing() {
        Path csv = dir.resolve("nope.csv");
        Path target = dir.resolve("out.txt");

        assertThatThrownBy(() -> service.importCsv(csv, target))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining(csv.toString())
                .hasCauseInstanceOf(NoSuchFileException.class);
        assertThat(target).doesNotExist();
    }
}
