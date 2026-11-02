package shopflow.w06;

public record Product(String sku, String name, String category, long priceCents) implements Comparable<Product> {

    @Override
    public int compareTo(Product other) {
        return sku.compareTo(other.sku);
    }
}
