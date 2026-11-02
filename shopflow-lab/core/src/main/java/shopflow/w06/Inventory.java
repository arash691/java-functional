package shopflow.w06;

import java.util.List;
import java.util.Set;

/**
 * SF-01 (extension): stock by SKU, low-stock list and sorted product views. Rules are in InventoryTest.
 */
public class Inventory {

    /** Registers a product with an initial stock. Throws IllegalArgumentException for a duplicate SKU or negative stock. */
    public void register(Product product, int initialStock) {
        throw new UnsupportedOperationException("TODO SF-01");
    }

    /** Adds stock. Throws IllegalArgumentException for an unknown SKU or qty <= 0. */
    public void add(String sku, int qty) {
        throw new UnsupportedOperationException("TODO SF-01");
    }

    /**
     * Removes stock. Throws IllegalArgumentException for an unknown SKU or qty <= 0 and
     * IllegalStateException("Insufficient stock for SKU: have H, need N") when there is not enough.
     */
    public void remove(String sku, int qty) {
        throw new UnsupportedOperationException("TODO SF-01");
    }

    /** Quantity in stock; 0 for an unknown SKU. */
    public int stockOf(String sku) {
        throw new UnsupportedOperationException("TODO SF-01");
    }

    /** Entries with quantity <= threshold, lowest quantity first, ties by SKU. Use a PriorityQueue. */
    public List<StockEntry> lowStock(int threshold) {
        throw new UnsupportedOperationException("TODO SF-01");
    }

    /** All products, cheapest first (ties by SKU). */
    public List<Product> productsSortedByPrice() {
        throw new UnsupportedOperationException("TODO SF-01");
    }

    /** All products by name A-Z (case-insensitive), then cheapest first. */
    public List<Product> productsSortedByNameThenPrice() {
        throw new UnsupportedOperationException("TODO SF-01");
    }

    /** Registered SKUs. The returned set must not allow callers to change the inventory. */
    public Set<String> skus() {
        throw new UnsupportedOperationException("TODO SF-01");
    }
}
