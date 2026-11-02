package shopflow.api.w12.ordering;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderView(Long id, String customerId, OrderStatus status, Instant createdAt,
                        List<OrderItemView> items, BigDecimal total) {
}
