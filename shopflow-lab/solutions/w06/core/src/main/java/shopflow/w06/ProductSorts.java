package shopflow.w06;

import java.util.Comparator;

public final class ProductSorts {
    private ProductSorts() {}

    public static Comparator<Product> byPrice() {
        return Comparator.comparingLong(Product::priceCents);
    }

    public static Comparator<Product> byNameThenPrice() {
        return Comparator.comparing(Product::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparingLong(Product::priceCents);
    }

    public static Comparator<Product> byPriceDescThenSku() {
        return Comparator.comparingLong(Product::priceCents).reversed().thenComparing(Product::sku);
    }
}
