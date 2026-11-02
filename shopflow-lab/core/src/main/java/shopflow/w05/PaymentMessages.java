package shopflow.w05;

/** EX-05-02: use a pattern-matching switch over the sealed {@link PaymentResult}. */
public final class PaymentMessages {
    private PaymentMessages() {}

    /**
     * Approved -> "Payment approved (tx T-1)"; Declined -> "Payment declined: reason";
     * Pending -> "Payment pending, retry in 90 seconds".
     */
    public static String describe(PaymentResult result) {
        return "TODO";
    }

    /** Approved and Declined are final answers; Pending is not. */
    public static boolean isFinal(PaymentResult result) {
        return false;
    }
}
