package shopflow.w05;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * SF-03 (continued): the order lifecycle. Rules are in OrderTest. Uses your OrderStatus from EX-05-01.
 */
public class Order {

    public record StatusChange(OrderStatus from, OrderStatus to) {}

    private final String id;
    private OrderStatus status = OrderStatus.CREATED;
    private final List<StatusChange> history = new ArrayList<>();
    private String transactionId;
    private int failedPayments;

    public Order(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public OrderStatus status() {
        return status;
    }

    public List<StatusChange> history() {
        return Collections.unmodifiableList(history);
    }

    public Optional<String> transactionId() {
        return Optional.ofNullable(transactionId);
    }

    public int failedPaymentAttempts() {
        return failedPayments;
    }

    public void applyPayment(PaymentResult result) {
        throw new UnsupportedOperationException("TODO SF-03");
    }

    public void ship() {
        throw new UnsupportedOperationException("TODO SF-03");
    }

    public void deliver() {
        throw new UnsupportedOperationException("TODO SF-03");
    }

    public void cancel() {
        throw new UnsupportedOperationException("TODO SF-03");
    }
}
