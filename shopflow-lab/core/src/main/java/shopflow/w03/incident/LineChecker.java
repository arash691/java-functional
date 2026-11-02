package shopflow.w03.incident;

/** Used by the nightly "duplicate cart" job to decide whether two lines carry the same amount. */
public final class LineChecker {

    private LineChecker() {}

    public static boolean sameQuantity(CartLine a, CartLine b) {
        return a.quantity() == b.quantity();
    }

    public static boolean isBulk(CartLine line) {
        return line.quantity() >= 100;
    }
}
