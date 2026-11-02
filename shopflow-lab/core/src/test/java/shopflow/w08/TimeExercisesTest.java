package shopflow.w08;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w08")
class TimeExercisesTest {

    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    @Nested
    @DisplayName("EX-08-01 ISO-8601 at the edges")
    class Iso {
        @Test
        void parsesIsoUtc() {
            Instant parsed = TimeExercises.fromIsoUtc("2025-03-09T07:00:00Z");
            assertThat(parsed).isEqualTo(ZonedDateTime.of(2025, 3, 9, 7, 0, 0, 0, ZoneOffset.UTC).toInstant());
        }

        @Test
        void formatsIsoUtc() {
            assertThat(TimeExercises.toIsoUtc(Instant.ofEpochSecond(0))).isEqualTo("1970-01-01T00:00:00Z");
        }

        @Test
        void roundTrips() {
            String text = "2025-11-02T06:30:15Z";
            assertThat(TimeExercises.toIsoUtc(TimeExercises.fromIsoUtc(text))).isEqualTo(text);
        }
    }

    @Nested
    @DisplayName("EX-08-02 which calendar day is it?")
    class CalendarDay {
        private final Instant lateEveningInNewYork = Instant.parse("2025-03-09T23:30:00Z");

        @Test
        void inNewYorkItIsStillTheNinth() {
            assertThat(TimeExercises.calendarDayOf(lateEveningInNewYork, NEW_YORK))
                    .as("19:30 EDT on March 9")
                    .isEqualTo(LocalDate.of(2025, 3, 9));
        }

        @Test
        void inTokyoItIsAlreadyTheTenth() {
            assertThat(TimeExercises.calendarDayOf(lateEveningInNewYork, TOKYO))
                    .as("08:30 JST on March 10")
                    .isEqualTo(LocalDate.of(2025, 3, 10));
        }
    }

    @Nested
    @DisplayName("EX-08-03 DST gap and overlap (New York, 2025)")
    class Dst {
        @Test
        void ordinaryTimeIsUnchanged() {
            ZonedDateTime result = TimeExercises.resolve(LocalDateTime.of(2025, 6, 1, 12, 0), NEW_YORK);
            assertThat(result.toLocalDateTime()).isEqualTo(LocalDateTime.of(2025, 6, 1, 12, 0));
            assertThat(result.getOffset()).isEqualTo(ZoneOffset.ofHours(-4));
        }

        @Test
        void springForwardGapMovesTheClockForward() {
            // 02:30 does not exist on 2025-03-09 in New York: clocks jump from 02:00 straight to 03:00.
            ZonedDateTime result = TimeExercises.resolve(LocalDateTime.of(2025, 3, 9, 2, 30), NEW_YORK);
            assertThat(result.toLocalDateTime()).isEqualTo(LocalDateTime.of(2025, 3, 9, 3, 30));
            assertThat(result.getOffset()).isEqualTo(ZoneOffset.ofHours(-4));
        }

        @Test
        void fallBackOverlapDefaultsToTheEarlierOccurrence() {
            // 01:30 happens twice on 2025-11-02. The default is the first one (still summer time).
            ZonedDateTime result = TimeExercises.resolve(LocalDateTime.of(2025, 11, 2, 1, 30), NEW_YORK);
            assertThat(result.getOffset()).isEqualTo(ZoneOffset.ofHours(-4));
        }

        @Test
        void fallBackOverlapCanPickTheLaterOccurrence() {
            ZonedDateTime result = TimeExercises.laterOverlap(LocalDateTime.of(2025, 11, 2, 1, 30), NEW_YORK);
            assertThat(result.toLocalDateTime()).isEqualTo(LocalDateTime.of(2025, 11, 2, 1, 30));
            assertThat(result.getOffset()).isEqualTo(ZoneOffset.ofHours(-5));
        }
    }

    @Nested
    @DisplayName("EX-08-04 \"a day later\" has two meanings")
    class DayLater {
        @Test
        void springForwardDayIsOnly23HoursLong() {
            ZonedDateTime saturdayNoon = ZonedDateTime.of(2025, 3, 8, 12, 0, 0, 0, NEW_YORK);

            assertThat(TimeExercises.sameLocalTimeTomorrow(saturdayNoon).toLocalDateTime())
                    .isEqualTo(LocalDateTime.of(2025, 3, 9, 12, 0));
            assertThat(TimeExercises.exactly24HoursLater(saturdayNoon).toLocalDateTime())
                    .as("24 elapsed hours across the spring-forward night land at 13:00 on the wall clock")
                    .isEqualTo(LocalDateTime.of(2025, 3, 9, 13, 0));
        }

        @Test
        void fallBackDayIs25HoursLong() {
            ZonedDateTime saturdayNoon = ZonedDateTime.of(2025, 11, 1, 12, 0, 0, 0, NEW_YORK);

            assertThat(TimeExercises.sameLocalTimeTomorrow(saturdayNoon).toLocalDateTime())
                    .isEqualTo(LocalDateTime.of(2025, 11, 2, 12, 0));
            assertThat(TimeExercises.exactly24HoursLater(saturdayNoon).toLocalDateTime())
                    .as("24 elapsed hours across the fall-back night land at 11:00 on the wall clock")
                    .isEqualTo(LocalDateTime.of(2025, 11, 2, 11, 0));
        }

        @Test
        void bothKeepTheZone() {
            ZonedDateTime t = ZonedDateTime.of(2025, 3, 8, 12, 0, 0, 0, NEW_YORK);
            assertThat(TimeExercises.sameLocalTimeTomorrow(t).getZone()).isEqualTo(NEW_YORK);
            assertThat(TimeExercises.exactly24HoursLater(t).getZone()).isEqualTo(NEW_YORK);
        }
    }

    @Nested
    @DisplayName("EX-08-05 inject a Clock")
    class Greeting {
        private final Clock sundayOnTheUtcClock = Clock.fixed(Instant.parse("2025-03-09T07:00:00Z"), ZoneOffset.UTC);

        @Test
        void earlyMorningInNewYork() {
            assertThat(TimeExercises.greeting(sundayOnTheUtcClock, NEW_YORK)).isEqualTo("Good morning");
        }

        @Test
        void sameInstantIsAfternoonInTokyo() {
            assertThat(TimeExercises.greeting(sundayOnTheUtcClock, TOKYO)).isEqualTo("Good afternoon");
        }

        @Test
        void eveningInNewYork() {
            Clock evening = Clock.fixed(Instant.parse("2025-03-09T23:30:00Z"), ZoneOffset.UTC);
            assertThat(TimeExercises.greeting(evening, NEW_YORK)).isEqualTo("Good evening");
        }

        @Test
        void theClocksOwnZoneIsIgnored() {
            Clock tokyoClock = Clock.fixed(Instant.parse("2025-03-09T07:00:00Z"), TOKYO);
            assertThat(TimeExercises.greeting(tokyoClock, NEW_YORK))
                    .as("the zone argument decides, not the clock's zone")
                    .isEqualTo("Good morning");
        }
    }

    @Nested
    @DisplayName("EX-08-06 human formatting")
    class Formatting {
        private final Instant instant = Instant.parse("2025-03-09T07:00:00Z");

        @Test
        void english() {
            assertThat(TimeExercises.format(instant, NEW_YORK, Locale.ENGLISH)).isEqualTo("9 Mar 2025 03:00");
        }

        @Test
        void frenchUsesFrenchMonthNames() {
            assertThat(TimeExercises.format(instant, NEW_YORK, Locale.FRENCH)).isEqualTo("9 mars 2025 03:00");
        }

        @Test
        void zoneChangesTheClockReading() {
            assertThat(TimeExercises.format(instant, TOKYO, Locale.ENGLISH)).isEqualTo("9 Mar 2025 16:00");
        }
    }
}
