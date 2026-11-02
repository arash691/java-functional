package shopflow.w06;

import java.util.List;
import java.util.Map;

/** EX-06-01: List, Set and Map in everyday shop code. */
public final class Collections101 {
    private Collections101() {}

    /** Removes duplicates but keeps the first-seen order: [b,a,b,c] -> [b,a,c]. */
    public static List<String> uniquePreserveOrder(List<String> skus) {
        return List.of();
    }

    /** How many times each SKU appears: [a,b,a] -> {a=2, b=1}. */
    public static Map<String, Integer> countBySku(List<String> skus) {
        return Map.of();
    }

    /**
     * Groups products by category. Categories appear in first-seen order and products
     * keep their input order within a category.
     */
    public static Map<String, List<Product>> groupByCategory(List<Product> products) {
        return Map.of();
    }
}
