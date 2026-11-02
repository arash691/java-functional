package shopflow.w06;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class Collections101 {
    private Collections101() {}

    public static List<String> uniquePreserveOrder(List<String> skus) {
        return new ArrayList<>(new LinkedHashSet<>(skus));
    }

    public static Map<String, Integer> countBySku(List<String> skus) {
        Map<String, Integer> counts = new HashMap<>();
        for (String sku : skus) {
            counts.merge(sku, 1, Integer::sum);
        }
        return counts;
    }

    public static Map<String, List<Product>> groupByCategory(List<Product> products) {
        Map<String, List<Product>> grouped = new LinkedHashMap<>();
        for (Product p : products) {
            grouped.computeIfAbsent(p.category(), c -> new ArrayList<>()).add(p);
        }
        return grouped;
    }
}
