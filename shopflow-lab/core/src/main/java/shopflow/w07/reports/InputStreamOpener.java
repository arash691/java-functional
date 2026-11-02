package shopflow.w07.reports;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@FunctionalInterface
public interface InputStreamOpener {
    InputStream open(Path path) throws IOException;
}
