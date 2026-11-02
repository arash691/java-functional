package shopflow.w08;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * SF-05 order timestamps. Acceptance tests: {@code OrderServiceTest}.
 *
 * <p>Rules (see your own docs/time-rules.md): store {@link Instant}, show it in the user's zone,
 * never call {@code now()} without a {@link Clock}.
 */
public class OrderService {

    private final Clock clock;

    public OrderService(Clock clock) {
        this.clock = clock;
    }

    /** Creates an order stamped with the clock's current instant. */
    public Order place(String id) {
        return new Order(id, Instant.EPOCH);
    }

    /** Shows the placement time in the user's zone, like {@code 2025-03-09 03:00:00 -04:00}. */
    public String displayFor(Order order, ZoneId userZone) {
        return "TODO";
    }

    /** The delivery promise: exactly 24 elapsed hours after placement. */
    public Instant promisedBy(Order order) {
        return order.placedAt();
    }

    /** A friendlier promise: the same wall-clock time on the next calendar day in {@code userZone}. */
    public Instant nextDaySameLocalTime(Order order, ZoneId userZone) {
        return order.placedAt();
    }

    /** True when delivery happened after {@link #promisedBy}. */
    public boolean isLate(Order order, Instant deliveredAt) {
        return false;
    }
}
