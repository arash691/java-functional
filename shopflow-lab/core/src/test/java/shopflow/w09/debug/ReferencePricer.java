package shopflow.w09.debug;

/**
 * The finance team's trusted spreadsheet logic, copied into code. Use it as an oracle while debugging:
 * for every line index it returns the price the business expects.
 */
public final class ReferencePricer {

    private ReferencePricer() {}

    public static long lineCents(int index) {
        long gross = BulkPricer.unitCents(index) * BulkPricer.quantity(index);
        return BulkPricer.quantity(index) >= 100 ? gross * 90 / 100 : gross;
    }

    public static long totalCents() {
        long total = 0;
        for (int i = 1; i <= BulkPricer.LINES; i++) {
            total += lineCents(i);
        }
        return total;
    }
}
