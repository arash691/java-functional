package shopflow.api.w12.ordering;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import shopflow.api.w12.cart.CartEntity;
import shopflow.api.w12.cart.CartItemEntity;
import shopflow.api.w12.cart.CartRepository;

@Service
public class OrderService {

    private final CartRepository carts;
    private final OrderRepository orders;

    public OrderService(CartRepository carts, OrderRepository orders) {
        this.carts = carts;
        this.orders = orders;
    }

    @Transactional
    public OrderView placeOrder(String customerId) {
        CartEntity cart = carts.findByCustomerId(customerId)
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(() -> new EmptyCartException(customerId));

        OrderEntity order = new OrderEntity(customerId, OrderStatus.NEW, Instant.now());
        for (CartItemEntity item : cart.getItems()) {
            order.addItem(new OrderItemEntity(item.getProduct(), item.getQuantity(), item.getProduct().getPrice()));
        }
        orders.save(order);
        cart.getItems().clear();
        return OrderQueryService.toView(order);
    }
}
