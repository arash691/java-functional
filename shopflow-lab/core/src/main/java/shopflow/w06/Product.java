package shopflow.w06;

/**
 * A catalog product. Natural ordering is by SKU (EX-06-03: implement compareTo).
 */
public record Product(String sku, String name, String category, long priceCents) implements Comparable<Product> {

    @Override
    public int compareTo(Product other) {
        return 0;
    }
}
