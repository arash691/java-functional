package shopflow.w05;

import java.time.Duration;

/** Outcome of asking the payment gateway to charge a customer. Complete: nothing to change here. */
public sealed interface PaymentResult {

    record Approved(String transactionId) implements PaymentResult {}

    record Declined(String reason) implements PaymentResult {}

    record Pending(Duration retryAfter) implements PaymentResult {}
}
