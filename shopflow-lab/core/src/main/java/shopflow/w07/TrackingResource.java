package shopflow.w07;

import java.util.List;

/** A resource that records when it is closed. Provided: do not change. */
public class TrackingResource implements AutoCloseable {
    private final String name;
    private final List<String> log;
    private final boolean failOnClose;
    private int closeCount;

    public TrackingResource(String name, List<String> log) {
        this(name, log, false);
    }

    public TrackingResource(String name, List<String> log, boolean failOnClose) {
        this.name = name;
        this.log = log;
        this.failOnClose = failOnClose;
    }

    public String name() {
        return name;
    }

    public int closeCount() {
        return closeCount;
    }

    @Override
    public void close() {
        closeCount++;
        log.add("close:" + name);
        if (failOnClose) {
            throw new IllegalStateException("close failed: " + name);
        }
    }
}
