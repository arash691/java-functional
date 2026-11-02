package shopflow.w03.incident;

/** Totals for large B2B orders. Amounts are in integer cents. */
public final class BulkOrder {

    private BulkOrder() {}

    public static int totalCents(int unitPriceCents, int quantity) {
        return unitPriceCents * quantity;
    }

    public static int sumCents(int... lineTotalsCents) {
        int sum = 0;
        for (int total : lineTotalsCents) {
            sum += total;
        }
        return sum;
    }
}
