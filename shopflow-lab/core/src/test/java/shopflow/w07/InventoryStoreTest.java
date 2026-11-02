package shopflow.w07;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** SF-04 acceptance tests. */
@Tag("w07")
@DisplayName("SF-04 InventoryStore (JSON)")
class InventoryStoreTest {

    @TempDir
    Path dir;

    private final InventoryStore store = new InventoryStore();

    @Nested
    class Saving {
        @Test
        void roundTrip() {
            Path file = dir.resolve("stock.json");
            store.save(Map.of("A-1", 5, "B-2", 0), file);
            assertThat(store.load(file)).containsExactlyInAnyOrderEntriesOf(Map.of("A-1", 5, "B-2", 0));
        }

        @Test
        void writesVersionAndSortedKeys() throws IOException {
            Path file = dir.resolve("stock.json");
            store.save(Map.of("B-2", 1, "A-1", 2), file);
            String json = Files.readString(file);
            assertThat(json).contains("\"version\"").contains("\"stock\"");
            assertThat(json.indexOf("\"A-1\"")).isLessThan(json.indexOf("\"B-2\""));
        }

        @Test
        void createsParentDirectories() {
            Path file = dir.resolve("deep/er/stock.json");
            store.save(Map.of("A-1", 1), file);
            assertThat(file).exists();
        }

        @Test
        @DisplayName("IO problems become a StoreException that keeps the cause")
        void ioProblemsAreTranslated() throws IOException {
            Path regularFile = Files.writeString(dir.resolve("regular"), "x");
            Path impossible = regularFile.resolve("stock.json"); // a file used as a directory
            assertThatThrownBy(() -> store.save(Map.of("A", 1), impossible))
                    .isInstanceOf(StoreException.class)
                    .hasCauseInstanceOf(IOException.class);
        }
    }

    @Nested
    class Loading {
        @Test
        void missingFile() {
            Path file = dir.resolve("nope.json");
            assertThatThrownBy(() -> store.load(file))
                    .isInstanceOf(StoreException.class)
                    .hasMessageContaining("not found")
                    .hasMessageContaining("nope.json")
                    .hasCauseInstanceOf(NoSuchFileException.class);
        }

        @Test
        void invalidJson() throws IOException {
            Path file = Files.writeString(dir.resolve("bad.json"), "{not json");
            assertThatThrownBy(() -> store.load(file))
                    .isInstanceOf(StoreException.class)
                    .hasMessageContaining("corrupt")
                    .hasMessageContaining("bad.json")
                    .hasCauseInstanceOf(JsonProcessingException.class);
        }

        @Test
        void emptyFile() throws IOException {
            Path file = Files.writeString(dir.resolve("empty.json"), "");
            assertThatThrownBy(() -> store.load(file))
                    .isInstanceOf(StoreException.class)
                    .hasMessageContaining("corrupt");
        }

        @Test
        void missingStockSection() throws IOException {
            Path file = Files.writeString(dir.resolve("nostock.json"), "{\"version\":1}");
            assertThatThrownBy(() -> store.load(file))
                    .isInstanceOf(StoreException.class)
                    .hasMessageContaining("corrupt");
        }

        @Test
        void negativeQuantity() throws IOException {
            Path file = Files.writeString(dir.resolve("neg.json"), "{\"version\":1,\"stock\":{\"A\":-3}}");
            assertThatThrownBy(() -> store.load(file))
                    .isInstanceOf(StoreException.class)
                    .hasMessageContaining("corrupt");
        }

        @Test
        void unsupportedVersion() throws IOException {
            Path file = Files.writeString(dir.resolve("v2.json"), "{\"version\":2,\"stock\":{}}");
            assertThatThrownBy(() -> store.load(file))
                    .isInstanceOf(StoreException.class)
                    .hasMessageContaining("unsupported version");
        }
    }
}
