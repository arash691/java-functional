package shopflow.w03.incident;

public final class BulkOrder {

    private BulkOrder() {}

    public static int totalCents(int unitPriceCents, int quantity) {
        return Math.multiplyExact(unitPriceCents, quantity);
    }

    public static int sumCents(int... lineTotalsCents) {
        int sum = 0;
        for (int total : lineTotalsCents) {
            sum = Math.addExact(sum, total);
        }
        return sum;
    }
}
