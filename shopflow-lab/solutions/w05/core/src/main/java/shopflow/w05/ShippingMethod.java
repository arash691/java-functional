package shopflow.w05;

public abstract class ShippingMethod {

    public abstract String name();

    protected abstract long costCents(int weightGrams);

    public final String quote(int weightGrams) {
        if (weightGrams <= 0) {
            throw new IllegalArgumentException("weightGrams must be positive but was " + weightGrams);
        }
        long cents = costCents(weightGrams);
        return String.format("%s: %d.%02d", name(), cents / 100, cents % 100);
    }
}
