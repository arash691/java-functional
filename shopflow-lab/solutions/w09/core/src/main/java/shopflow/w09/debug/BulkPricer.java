package shopflow.w09.debug;

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
        return quantity(index) >= 100 ? gross * 90 / 100 : gross;
    }

    public static long totalCents() {
        long total = 0;
        for (int i = 1; i <= LINES; i++) {
            total += lineCents(i);
        }
        return total;
    }
}
