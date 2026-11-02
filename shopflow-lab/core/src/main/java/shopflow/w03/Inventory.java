package shopflow.w03;

/**
 * SF-01 (first part): the stock of each SKU.
 * Hint: java.util.HashMap<String, Integer> is enough; see the lesson for a 5-line primer.
 *
 * Rules (see InventoryTest for the exact expectations):
 *  - add(sku, qty) increases stock; qty must be > 0, sku must not be null or blank
 *  - remove(sku, qty) decreases stock; qty must be > 0; removing more than available
 *    throws IllegalStateException whose message contains the SKU
 *  - stockOf(sku) is 0 for a SKU never seen
 *  - distinctSkus() counts SKUs with stock > 0
 */
public class Inventory {

    public void add(String sku, int quantity) {
        throw new UnsupportedOperationException("TODO SF-01 add");
    }

    public void remove(String sku, int quantity) {
        throw new UnsupportedOperationException("TODO SF-01 remove");
    }

    public int stockOf(String sku) {
        throw new UnsupportedOperationException("TODO SF-01 stockOf");
    }

    public int distinctSkus() {
        throw new UnsupportedOperationException("TODO SF-01 distinctSkus");
    }
}
