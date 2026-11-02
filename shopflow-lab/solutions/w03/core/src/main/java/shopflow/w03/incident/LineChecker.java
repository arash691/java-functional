package shopflow.w03.incident;

public final class LineChecker {

    private LineChecker() {}

    public static boolean sameQuantity(CartLine a, CartLine b) {
        return a.quantity().equals(b.quantity());
    }

    public static boolean isBulk(CartLine line) {
        return line.quantity() >= 100;
    }
}
