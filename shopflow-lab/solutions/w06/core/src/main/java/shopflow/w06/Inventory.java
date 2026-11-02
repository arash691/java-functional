package shopflow.w06;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Inventory {
    private final Map<String, Product> products = new LinkedHashMap<>();
    private final Map<String, Integer> stock = new HashMap<>();

    public void register(Product product, int initialStock) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("initialStock must not be negative");
        }
        if (products.putIfAbsent(product.sku(), product) != null) {
            throw new IllegalArgumentException("Duplicate SKU: " + product.sku());
        }
        stock.put(product.sku(), initialStock);
    }

    public void add(String sku, int qty) {
        requireKnown(sku);
        requirePositive(qty);
        stock.merge(sku, qty, Integer::sum);
    }

    public void remove(String sku, int qty) {
        requireKnown(sku);
        requirePositive(qty);
        int have = stock.get(sku);
        if (have < qty) {
            throw new IllegalStateException("Insufficient stock for " + sku + ": have " + have + ", need " + qty);
        }
        stock.put(sku, have - qty);
    }

    public int stockOf(String sku) {
        return stock.getOrDefault(sku, 0);
    }

    public List<StockEntry> lowStock(int threshold) {
        PriorityQueue<StockEntry> queue = new PriorityQueue<>(
                Comparator.comparingInt(StockEntry::quantity).thenComparing(StockEntry::sku));
        for (Map.Entry<String, Integer> e : stock.entrySet()) {
            if (e.getValue() <= threshold) {
                queue.add(new StockEntry(e.getKey(), e.getValue()));
            }
        }
        List<StockEntry> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            result.add(queue.poll());
        }
        return result;
    }

    public List<Product> productsSortedByPrice() {
        List<Product> list = new ArrayList<>(products.values());
        list.sort(ProductSorts.byPrice().thenComparing(Product::sku));
        return list;
    }

    public List<Product> productsSortedByNameThenPrice() {
        List<Product> list = new ArrayList<>(products.values());
        list.sort(ProductSorts.byNameThenPrice());
        return list;
    }

    public Set<String> skus() {
        return Collections.unmodifiableSet(products.keySet());
    }

    private void requireKnown(String sku) {
        if (!products.containsKey(sku)) {
            throw new IllegalArgumentException("Unknown SKU: " + sku);
        }
    }

    private static void requirePositive(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be positive but was " + qty);
        }
    }
}
