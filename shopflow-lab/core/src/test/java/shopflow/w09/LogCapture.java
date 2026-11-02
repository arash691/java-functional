package shopflow.w09;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * Test helper: captures what a class logs, so tests can assert on log output.
 * Use in try-with-resources. It forces the logger to DEBUG so results do not depend on any logback.xml.
 */
public final class LogCapture implements AutoCloseable {

    private final Logger logger;
    private final Level originalLevel;
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    public LogCapture(Class<?> loggedClass) {
        this.logger = (Logger) LoggerFactory.getLogger(loggedClass);
        this.originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        appender.start();
        logger.addAppender(appender);
    }

    public List<ILoggingEvent> events() {
        return appender.list;
    }

    /** Fully formatted messages, e.g. {@code Order placed: id=A-1 cents=1250}. */
    public List<String> messages() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    @Override
    public void close() {
        logger.detachAppender(appender);
        logger.setLevel(originalLevel);
        appender.stop();
    }
}
