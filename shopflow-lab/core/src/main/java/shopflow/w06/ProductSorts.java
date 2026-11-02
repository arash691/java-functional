package shopflow.w06;

import java.util.Comparator;

/** EX-06-03: Comparator vs Comparable. Build the comparators with Comparator.comparing(...).thenComparing(...). */
public final class ProductSorts {
    private ProductSorts() {}

    /** Cheapest first; ties keep their order (do not add a tie-breaker). */
    public static Comparator<Product> byPrice() {
        return (a, b) -> 0;
    }

    /** Name A-Z (case-insensitive), then cheapest first. */
    public static Comparator<Product> byNameThenPrice() {
        return (a, b) -> 0;
    }

    /** Most expensive first; ties broken by SKU A-Z. */
    public static Comparator<Product> byPriceDescThenSku() {
        return (a, b) -> 0;
    }
}
