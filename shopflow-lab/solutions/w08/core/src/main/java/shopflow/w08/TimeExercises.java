package shopflow.w08;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class TimeExercises {

    private TimeExercises() {}

    public static Instant fromIsoUtc(String iso) {
        return Instant.parse(iso);
    }

    public static String toIsoUtc(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }

    public static LocalDate calendarDayOf(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalDate();
    }

    public static ZonedDateTime resolve(LocalDateTime local, ZoneId zone) {
        return local.atZone(zone);
    }

    public static ZonedDateTime laterOverlap(LocalDateTime local, ZoneId zone) {
        return local.atZone(zone).withLaterOffsetAtOverlap();
    }

    public static ZonedDateTime sameLocalTimeTomorrow(ZonedDateTime time) {
        return time.plusDays(1);
    }

    public static ZonedDateTime exactly24HoursLater(ZonedDateTime time) {
        return time.plusHours(24);
    }

    public static String greeting(Clock clock, ZoneId zone) {
        int hour = clock.instant().atZone(zone).getHour();
        if (hour < 12) {
            return "Good morning";
        }
        return hour < 18 ? "Good afternoon" : "Good evening";
    }

    public static String format(Instant instant, ZoneId zone, Locale locale) {
        return DateTimeFormatter.ofPattern("d MMM uuuu HH:mm", locale).withZone(zone).format(instant);
    }
}
