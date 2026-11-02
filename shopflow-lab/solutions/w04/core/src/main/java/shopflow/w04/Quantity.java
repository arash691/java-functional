package shopflow.w04;

public final class Quantity {

    public static final Quantity ZERO = new Quantity(0);

    private final int value;

    private Quantity(int value) {
        this.value = value;
    }

    public static Quantity of(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("quantity must be >= 0: " + value);
        }
        return new Quantity(value);
    }

    public Quantity plus(Quantity other) {
        return new Quantity(Math.addExact(value, other.value));
    }

    public int value() {
        return value;
    }
}
