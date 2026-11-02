package shopflow.w07.reports;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Test helper: records close() and can fail with an IOException after N bytes. */
class TrackingInputStream extends InputStream {
    private final ByteArrayInputStream in;
    private final int failAfterBytes;
    private int bytesRead;
    private boolean closed;

    TrackingInputStream(String content) {
        this(content, Integer.MAX_VALUE);
    }

    TrackingInputStream(String content, int failAfterBytes) {
        this.in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        this.failAfterBytes = failAfterBytes;
    }

    @Override
    public int read() throws IOException {
        if (bytesRead >= failAfterBytes) {
            throw new IOException("disk error");
        }
        int b = in.read();
        if (b >= 0) {
            bytesRead++;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (bytesRead >= failAfterBytes) {
            throw new IOException("disk error");
        }
        int n = in.read(b, off, Math.min(len, failAfterBytes - bytesRead));
        if (n > 0) {
            bytesRead += n;
        }
        return n;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        in.close();
    }

    boolean isClosed() {
        return closed;
    }
}
