package shopflow.w08;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** SF-05 acceptance tests. Read them first: they are the requirements. */
@Tag("w08")
class OrderServiceTest {

    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    /** A clock the test can move forward by hand. */
    static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advanceSeconds(long seconds) {
            now = now.plusSeconds(seconds);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @Nested
    @DisplayName("placing an order")
    class Placing {
        @Test
        void keepsTheId() {
            OrderService service = new OrderService(Clock.systemUTC());
            assertThat(service.place("A-1").id()).isEqualTo("A-1");
        }

        @Test
        void stampsTheClocksInstant() {
            Instant fixed = Instant.parse("2025-03-09T07:00:00Z");
            OrderService service = new OrderService(Clock.fixed(fixed, ZoneOffset.UTC));
            assertThat(service.place("A-1").placedAt()).isEqualTo(fixed);
        }

        @Test
        void followsTheClockAsTimePasses() {
            MutableClock clock = new MutableClock(Instant.parse("2025-03-09T07:00:00Z"));
            OrderService service = new OrderService(clock);

            Order first = service.place("A-1");
            clock.advanceSeconds(90);
            Order second = service.place("A-2");

            assertThat(second.placedAt()).isEqualTo(first.placedAt().plusSeconds(90));
        }
    }

    @Nested
    @DisplayName("displaying in the user's zone (spring-forward night, New York 2025-03-09)")
    class Displaying {
        private final OrderService service = new OrderService(Clock.systemUTC());

        @Test
        void oneSecondBeforeTheJump() {
            Order order = new Order("A-1", Instant.parse("2025-03-09T06:59:59Z"));
            assertThat(service.displayFor(order, NEW_YORK)).isEqualTo("2025-03-09 01:59:59 -05:00");
        }

        @Test
        void atTheJump() {
            Order order = new Order("A-1", Instant.parse("2025-03-09T07:00:00Z"));
            assertThat(service.displayFor(order, NEW_YORK)).isEqualTo("2025-03-09 03:00:00 -04:00");
        }

        @Test
        void tokyoHasNoDst() {
            Order order = new Order("A-1", Instant.parse("2025-03-09T07:00:00Z"));
            assertThat(service.displayFor(order, TOKYO)).isEqualTo("2025-03-09 16:00:00 +09:00");
        }
    }

    @Nested
    @DisplayName("delivery promise across the spring-forward night")
    class Promise {
        private final OrderService service = new OrderService(Clock.systemUTC());
        // 2025-03-08 12:00 in New York (EST, UTC-5)
        private final Order saturdayNoon = new Order("A-1", Instant.parse("2025-03-08T17:00:00Z"));

        @Test
        void exactly24ElapsedHours() {
            assertThat(service.promisedBy(saturdayNoon)).isEqualTo(Instant.parse("2025-03-09T17:00:00Z"));
        }

        @Test
        void sameWallClockTimeTomorrowIsOnly23HoursAway() {
            assertThat(service.nextDaySameLocalTime(saturdayNoon, NEW_YORK))
                    .isEqualTo(Instant.parse("2025-03-09T16:00:00Z"));
        }

        @Test
        void lateMeansAfterThePromise() {
            assertThat(service.isLate(saturdayNoon, Instant.parse("2025-03-09T17:00:00Z")))
                    .as("exactly on time is not late")
                    .isFalse();
            assertThat(service.isLate(saturdayNoon, Instant.parse("2025-03-09T17:00:01Z")))
                    .as("one second after the promise is late")
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("persistence as JSON")
    class Json {
        private final Order order = new Order("A-1", Instant.parse("2025-03-09T07:00:00Z"));

        @Test
        void writesTheTimestampAsAnIsoUtcString() {
            assertThat(OrderJson.toJson(order)).contains("\"placedAt\":\"2025-03-09T07:00:00Z\"");
        }

        @Test
        void roundTrips() {
            assertThat(OrderJson.fromJson(OrderJson.toJson(order))).isEqualTo(order);
        }
    }
}
