package shopflow.w03;

/** EX-03-01: arithmetic, integer division, overloading. Money is in integer cents for now. */
public final class Pricing {

    private Pricing() {}

    /** unit price (cents) times quantity. A negative quantity is an IllegalArgumentException. */
    public static int lineTotalCents(int unitPriceCents, int quantity) {
        throw new UnsupportedOperationException("TODO EX-03-01 lineTotalCents");
    }

    /**
     * Applies a percentage discount. The discount amount is cents * percent / 100 using
     * integer division (fractions of a cent are dropped from the discount).
     * percent outside 0..100 is an IllegalArgumentException.
     */
    public static int applyDiscountPercent(int cents, int percent) {
        throw new UnsupportedOperationException("TODO EX-03-01 applyDiscountPercent");
    }

    /** 1999 -> "19.99", 5 -> "0.05", 0 -> "0.00". */
    public static String format(int cents) {
        throw new UnsupportedOperationException("TODO EX-03-01 format(int)");
    }

    /** Overload with a currency code: format(1999, "EUR") -> "EUR 19.99". */
    public static String format(int cents, String currencyCode) {
        throw new UnsupportedOperationException("TODO EX-03-01 format(int, String)");
    }
}
