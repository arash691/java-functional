package shopflow.w08;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class OrderService {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss xxx");

    private final Clock clock;

    public OrderService(Clock clock) {
        this.clock = clock;
    }

    public Order place(String id) {
        return new Order(id, clock.instant());
    }

    public String displayFor(Order order, ZoneId userZone) {
        return DISPLAY.format(order.placedAt().atZone(userZone));
    }

    public Instant promisedBy(Order order) {
        return order.placedAt().plus(Duration.ofHours(24));
    }

    public Instant nextDaySameLocalTime(Order order, ZoneId userZone) {
        return order.placedAt().atZone(userZone).plusDays(1).toInstant();
    }

    public boolean isLate(Order order, Instant deliveredAt) {
        return deliveredAt.isAfter(promisedBy(order));
    }
}
