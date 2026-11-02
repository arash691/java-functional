package shopflow.api.w12.cart;

import org.springframework.stereotype.Service;

/**
 * SF-08. Persist carts in Postgres. Create (you):
 *   - Flyway migration V4__create_cart.sql with tables cart(id, customer_id unique) and cart_item(id, cart_id, product_id, quantity)
 *   - entities, repository, and the three methods below.
 * Rules: adding the same SKU again adds to the quantity (one line per SKU); quantity must be > 0;
 * unknown SKU -> UnknownProductException; a customer without a cart has an empty cart with total 0.00.
 */
@Service
public class CartService {

    public void addItem(String customerId, String sku, int quantity) {
        throw new UnsupportedOperationException("TODO SF-08");
    }

    public CartView getCart(String customerId) {
        throw new UnsupportedOperationException("TODO SF-08");
    }

    public void clear(String customerId) {
        throw new UnsupportedOperationException("TODO SF-08");
    }
}
