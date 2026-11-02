package shopflow.w04;

/**
 * EX-04-02: private constructor + static factory.
 *  - Quantity.of(n) for n >= 0, otherwise IllegalArgumentException
 *  - Quantity.ZERO constant
 *  - plus(other) returns a new Quantity; value() returns the int
 */
public final class Quantity {

    public static final Quantity ZERO = new Quantity(0);

    private Quantity(int value) {
        // TODO EX-04-02
    }

    public static Quantity of(int value) {
        return ZERO; // TODO EX-04-02
    }

    public Quantity plus(Quantity other) {
        return this; // TODO EX-04-02
    }

    public int value() {
        return -1; // TODO EX-04-02
    }
}
