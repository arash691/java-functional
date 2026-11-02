package shopflow.w09;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Week 9 worked example. Already green: run {@code main}, read the console, then change levels and messages.
 *
 * <p>SLF4J is the API you code against. Logback is the engine that prints. Without any config file Logback
 * prints everything from DEBUG up in a default pattern; you will write your own {@code logback.xml} in S1.
 */
public class LoggingTour {

    private static final Logger log = LoggerFactory.getLogger(LoggingTour.class);

    public static void main(String[] args) {
        log.trace("TRACE: finest detail, usually off");
        log.debug("DEBUG: developer detail");
        log.info("INFO: normal business events");
        log.warn("WARN: something odd, but we carry on");
        log.error("ERROR: something failed");

        // Parameterised message: the string is only built if INFO is enabled.
        String orderId = "A-1";
        long cents = 1250;
        log.info("Order placed: id={} cents={}", orderId, cents);

        // MDC: context that travels with every log line written by this thread.
        MDC.put("requestId", "req-42");
        try {
            log.info("Inside a request (add %X{requestId} to your pattern to see the id)");
        } finally {
            MDC.clear();
        }

        // An exception goes LAST and is not part of the message: SLF4J prints the stack trace for you.
        try {
            Integer.parseInt("oops");
        } catch (NumberFormatException e) {
            log.error("Could not read quantity for order {}", orderId, e);
        }
    }
}
