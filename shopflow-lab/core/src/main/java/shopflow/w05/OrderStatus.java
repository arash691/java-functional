package shopflow.w05;

import java.util.Set;

/**
 * Lifecycle of an order. EX-05-01: make the three methods below work.
 *
 * <pre>
 * CREATED -> PAID -> SHIPPED -> DELIVERED
 *    \         \
 *     +---------+--> CANCELLED   (only before SHIPPED)
 * </pre>
 */
public enum OrderStatus {
    CREATED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        return false;
    }

    public Set<OrderStatus> allowedNext() {
        return Set.of();
    }

    public boolean isTerminal() {
        return false;
    }
}
