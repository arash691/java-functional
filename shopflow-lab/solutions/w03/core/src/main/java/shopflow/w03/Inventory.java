package shopflow.w03;

import java.util.HashMap;
import java.util.Map;

public class Inventory {

    private final Map<String, Integer> stock = new HashMap<>();

    public void add(String sku, int quantity) {
        requireSku(sku);
        requirePositive(quantity);
        stock.merge(sku, quantity, Integer::sum);
    }

    public void remove(String sku, int quantity) {
        requireSku(sku);
        requirePositive(quantity);
        int current = stockOf(sku);
        if (quantity > current) {
            throw new IllegalStateException("Not enough stock for " + sku + ": have " + current + ", need " + quantity);
        }
        if (quantity == current) {
            stock.remove(sku);
        } else {
            stock.put(sku, current - quantity);
        }
    }

    public int stockOf(String sku) {
        return stock.getOrDefault(sku, 0);
    }

    public int distinctSkus() {
        return stock.size();
    }

    private static void requireSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0: " + quantity);
        }
    }
}
