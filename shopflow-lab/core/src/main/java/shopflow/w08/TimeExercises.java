package shopflow.w08;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

/**
 * Week 8, S1 exercises. Each method has a matching nested test class in {@code TimeExercisesTest}.
 * Work top to bottom: every exercise turns a few more tests green.
 */
public final class TimeExercises {

    private TimeExercises() {}

    // ---- EX-08-01: ISO-8601 at the edges -------------------------------------------------------

    /** Parses an ISO-8601 UTC timestamp such as {@code 2025-03-09T07:00:00Z}. */
    public static Instant fromIsoUtc(String iso) {
        throw new UnsupportedOperationException("TODO EX-08-01");
    }

    /** Formats an instant as ISO-8601 UTC, e.g. {@code 1970-01-01T00:00:00Z}. */
    public static String toIsoUtc(Instant instant) {
        throw new UnsupportedOperationException("TODO EX-08-01");
    }

    // ---- EX-08-02: which calendar day is it? ---------------------------------------------------

    /** The calendar date of {@code instant} for a person living in {@code zone}. */
    public static LocalDate calendarDayOf(Instant instant, ZoneId zone) {
        throw new UnsupportedOperationException("TODO EX-08-02");
    }

    // ---- EX-08-03: DST gap and overlap ---------------------------------------------------------

    /** Attaches {@code zone} to a wall-clock reading using the zone's default rules. */
    public static ZonedDateTime resolve(LocalDateTime local, ZoneId zone) {
        throw new UnsupportedOperationException("TODO EX-08-03");
    }

    /** Like {@link #resolve} but, when the wall-clock time occurs twice, picks the LATER occurrence. */
    public static ZonedDateTime laterOverlap(LocalDateTime local, ZoneId zone) {
        throw new UnsupportedOperationException("TODO EX-08-03");
    }

    // ---- EX-08-04: "a day later" has two meanings ----------------------------------------------

    /** Same wall-clock time on the next calendar day (a "calendar" day, possibly 23 or 25 hours). */
    public static ZonedDateTime sameLocalTimeTomorrow(ZonedDateTime time) {
        throw new UnsupportedOperationException("TODO EX-08-04");
    }

    /** Exactly 24 hours of elapsed time later (the wall clock may show a different time). */
    public static ZonedDateTime exactly24HoursLater(ZonedDateTime time) {
        throw new UnsupportedOperationException("TODO EX-08-04");
    }

    // ---- EX-08-05: inject a Clock --------------------------------------------------------------

    /**
     * "Good morning" before 12:00, "Good afternoon" before 18:00, otherwise "Good evening", judged by the
     * wall clock in {@code zone}. Must not call {@code now()} without a clock.
     */
    public static String greeting(Clock clock, ZoneId zone) {
        throw new UnsupportedOperationException("TODO EX-08-05");
    }

    // ---- EX-08-06: human formatting ------------------------------------------------------------

    /** Formats like {@code 9 Mar 2025 03:00} (pattern {@code d MMM uuuu HH:mm}) in the given zone and locale. */
    public static String format(Instant instant, ZoneId zone, Locale locale) {
        throw new UnsupportedOperationException("TODO EX-08-06");
    }
}
