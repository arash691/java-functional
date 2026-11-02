package shopflow.w05;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Order " + id + " is " + status + ", payment not accepted");
        }
        switch (result) {
            case PaymentResult.Approved a -> {
                transitionTo(OrderStatus.PAID);
                transactionId = a.transactionId();
            }
            case PaymentResult.Declined d -> failedPayments++;
            case PaymentResult.Pending p -> { }
        }
    }

    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
    }

    public void deliver() {
        transitionTo(OrderStatus.DELIVERED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    private void transitionTo(OrderStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException("Cannot go from " + status + " to " + next);
        }
        history.add(new StatusChange(status, next));
        status = next;
    }
}
