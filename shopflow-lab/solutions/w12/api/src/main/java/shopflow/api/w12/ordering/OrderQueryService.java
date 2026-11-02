package shopflow.api.w12.ordering;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryService {

    private final OrderRepository orders;

    public OrderQueryService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public List<OrderView> listWithItems() {
        return orders.findAllWithItems().stream().map(OrderQueryService::toView).toList();
    }

    /** The mapping touches lazy associations, so it must run inside a transaction. */
    @Transactional(readOnly = true)
    public OrderView describe(Long id) {
        OrderEntity order = orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return toView(order);
    }

    static OrderView toView(OrderEntity order) {
        List<OrderItemView> items = order.getItems().stream()
                .map(i -> new OrderItemView(i.getProduct().getSku(), i.getProduct().getName(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        BigDecimal total = items.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderView(order.getId(), order.getCustomerId(), order.getStatus(), order.getCreatedAt(), items, total);
    }
}
