package shopflow.w05;

/**
 * EX-05-03: abstract class with a template method.
 * Subclasses supply {@link #name()} and {@link #costCents(int)}; {@link #quote(int)} is the same for all.
 */
public abstract class ShippingMethod {

    public abstract String name();

    /** Cost in cents for a parcel of the given weight (weight is already validated). */
    protected abstract long costCents(int weightGrams);

    /**
     * Template method. Throws IllegalArgumentException when weightGrams <= 0.
     * Otherwise returns e.g. "Standard: 5.00" (name, colon, space, euros.cents).
     */
    public final String quote(int weightGrams) {
        throw new UnsupportedOperationException("TODO EX-05-03");
    }
}
