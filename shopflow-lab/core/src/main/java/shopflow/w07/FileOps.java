package shopflow.w07;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** EX-07-03: java.nio.file in practice. Always UTF-8. Use Files and Path, not java.io.File. */
public final class FileOps {
    private FileOps() {}

    /** Writes the lines (UTF-8), replacing an existing file and creating missing parent directories. */
    public static void writeLines(Path file, List<String> lines) throws IOException {
    }

    /** Reads all lines (UTF-8). A missing file must surface as NoSuchFileException. */
    public static List<String> readLines(Path file) throws IOException {
        return List.of();
    }

    /** Number of lines containing the text (case-sensitive). */
    public static long countLinesContaining(Path file, String text) throws IOException {
        return 0;
    }

    /** Copies a text file, overwriting the target. */
    public static void copyUtf8(Path from, Path to) throws IOException {
    }
}
