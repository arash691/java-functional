package shopflow.api.w12.ordering;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read side for the order screens. */
@Service
public class OrderQueryService {

    private final OrderRepository orders;

    public OrderQueryService(OrderRepository orders) {
        this.orders = orders;
    }

    /** All orders with their items, for the back-office list. */
    @Transactional(readOnly = true)
    public List<OrderView> listWithItems() {
        return orders.findAll().stream().map(this::toView).toList();
    }

    /** One order with its items. */
    public OrderView describe(Long id) {
        OrderEntity order = orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return toView(order);
    }

    private OrderView toView(OrderEntity order) {
        List<OrderItemView> items = order.getItems().stream()
                .map(i -> new OrderItemView(i.getProduct().getSku(), i.getProduct().getName(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        BigDecimal total = items.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderView(order.getId(), order.getCustomerId(), order.getStatus(), order.getCreatedAt(), items, total);
    }
}
