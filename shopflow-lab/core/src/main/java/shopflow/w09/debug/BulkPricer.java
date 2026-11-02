package shopflow.w09.debug;

/**
 * Prices a catalogue of 1,000 order lines. Business rule: lines of 100 units or more get 10% off.
 * The finance team says the grand total is wrong. Nobody knows which lines are wrong.
 */
public final class BulkPricer {

    public static final int LINES = 1_000;

    private BulkPricer() {}

    public static long unitCents(int index) {
        return 100 + (index * 7L) % 90;
    }

    public static int quantity(int index) {
        return (int) ((index * 37L) % 250) + 1;
    }

    public static long lineCents(int index) {
        long gross = unitCents(index) * quantity(index);
        return quantity(index) > 100 ? gross * 90 / 100 : gross;
    }

    public static long totalCents() {
        long total = 0;
        for (int i = 1; i <= LINES; i++) {
            total += lineCents(i);
        }
        return total;
    }
}
