package shopflow.w04.incident;

import java.util.HashSet;
import java.util.Set;

/** The customer's cart: each product appears once. */
public class Cart {

    private final Set<ProductListing> items = new HashSet<>();

    /** @return true if the product was newly added. */
    public boolean add(ProductListing listing) {
        return items.add(listing);
    }

    public boolean contains(ProductListing listing) {
        return items.contains(listing);
    }

    public boolean remove(ProductListing listing) {
        return items.remove(listing);
    }

    public int size() {
        return items.size();
    }
}
