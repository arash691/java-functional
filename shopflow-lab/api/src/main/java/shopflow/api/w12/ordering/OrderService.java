package shopflow.api.w12.ordering;

import org.springframework.stereotype.Service;

/**
 * SF-09. Turn the customer's cart into an order (status NEW, createdAt = now).
 * Rules: unit price is a snapshot of the product price at order time; the cart is emptied; an empty cart ->
 * EmptyCartException and NOTHING is stored; everything happens in one transaction.
 * (Hint: use @Transactional, CartService for the cart and your ProductRepository.findBySku from EX-12-01.)
 */
@Service
public class OrderService {

    public OrderView placeOrder(String customerId) {
        throw new UnsupportedOperationException("TODO SF-09");
    }
}
