package shopflow.w05;

import shopflow.w05.PaymentResult.Approved;
import shopflow.w05.PaymentResult.Declined;
import shopflow.w05.PaymentResult.Pending;

public final class PaymentMessages {
    private PaymentMessages() {}

    public static String describe(PaymentResult result) {
        return switch (result) {
            case Approved a -> "Payment approved (tx " + a.transactionId() + ")";
            case Declined d -> "Payment declined: " + d.reason();
            case Pending p -> "Payment pending, retry in " + p.retryAfter().toSeconds() + " seconds";
        };
    }

    public static boolean isFinal(PaymentResult result) {
        return switch (result) {
            case Approved a -> true;
            case Declined d -> true;
            case Pending p -> false;
        };
    }
}
