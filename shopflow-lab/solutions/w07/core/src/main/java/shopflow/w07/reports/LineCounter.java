package shopflow.w07.reports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class LineCounter {
    private final InputStreamOpener opener;

    public LineCounter(InputStreamOpener opener) {
        this.opener = opener;
    }

    public int countLines(Path file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(opener.open(file), StandardCharsets.UTF_8))) {
            int lines = 0;
            while (reader.readLine() != null) {
                lines++;
            }
            return lines;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
