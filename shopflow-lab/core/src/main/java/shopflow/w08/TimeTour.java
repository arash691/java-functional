package shopflow.w08;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Week 8 worked example. Already green: run it, read it line by line, then change one thing at a time.
 *
 * <p>Run {@code main} from your IDE (the gutter arrow next to {@code main}).
 */
public class TimeTour {

    public static void main(String[] args) {
        // An Instant is a point on the global timeline. It has no zone and no "local" clock reading.
        Instant instant = Instant.parse("2025-03-09T07:00:00Z");
        System.out.println("Instant          : " + instant);

        // The same instant shown on three different wall clocks.
        for (String id : new String[] {"UTC", "America/New_York", "Asia/Tokyo"}) {
            System.out.println("In " + id + " : " + describe(instant, ZoneId.of(id)));
        }

        // A LocalDateTime is just a reading of a wall clock: it is NOT a point in time.
        LocalDateTime wallClock = LocalDateTime.of(2025, 3, 9, 2, 30);
        System.out.println("LocalDateTime    : " + wallClock + "   <- which instant is this? Nobody knows.");

        // Attaching a zone turns the wall-clock reading into an instant, using the zone's rules.
        ZonedDateTime inNewYork = wallClock.atZone(ZoneId.of("America/New_York"));
        System.out.println("02:30 in New York: " + inNewYork + "   <- predict this before you run it!");
    }

    /** Describes an instant as seen in a zone, e.g. {@code 2025-03-09T03:00-04:00[America/New_York]}. */
    static String describe(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toString();
    }
}
