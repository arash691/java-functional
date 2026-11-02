package shopflow.w09.review;

/** A stock keeping unit such as {@code SKU-0042}. */
public final class Sku {

    private final String value;

    public Sku(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Sku other && other.value.equals(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
