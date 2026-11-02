package shopflow.w03;

/**
 * EX-03-03: switch expressions.
 * Base cost by zone: EU 500, US 1200, ASIA 1500 (cents).
 * Weight surcharge: up to 1000 g: +0; up to 5000 g: +400; above 5000 g: +1200.
 * Unknown zone or weight <= 0: IllegalArgumentException.
 */
public final class Shipping {

    private Shipping() {}

    public static int costCents(String zone, int weightGrams) {
        throw new UnsupportedOperationException("TODO EX-03-03 costCents");
    }
}
