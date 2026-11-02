package shopflow.api.w12;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import shopflow.api.w12.catalog.ProductEntity;
import shopflow.api.w12.catalog.ProductRepository;
import shopflow.api.w12.ordering.OrderEntity;
import shopflow.api.w12.ordering.OrderItemEntity;
import shopflow.api.w12.ordering.OrderRepository;
import shopflow.api.w12.ordering.OrderStatus;

/** Test helper: inserts orders with items straight through the repositories. */
@Component
class OrderTestData {

    private final ProductRepository products;
    private final OrderRepository orders;
    private final TransactionTemplate tx;

    OrderTestData(ProductRepository products, OrderRepository orders, TransactionTemplate tx) {
        this.products = products;
        this.orders = orders;
        this.tx = tx;
    }

    /** @return the id of the first order created */
    Long createOrders(int orderCount, int itemsPerOrder) {
        List<ProductEntity> catalog = List.of(
                products.save(new ProductEntity("SKU-1", "Notebook", new BigDecimal("10.00"))),
                products.save(new ProductEntity("SKU-2", "Pen", new BigDecimal("5.00"))),
                products.save(new ProductEntity("SKU-3", "Ink", new BigDecimal("4.50"))));
        return tx.execute(status -> {
            Long first = null;
            for (int i = 0; i < orderCount; i++) {
                OrderEntity order = new OrderEntity("customer-" + i, OrderStatus.NEW, Instant.parse("2026-11-02T10:00:00Z"));
                for (int j = 0; j < itemsPerOrder; j++) {
                    ProductEntity p = catalog.get(j % catalog.size());
                    order.addItem(new OrderItemEntity(p, j + 1, p.getPrice()));
                }
                OrderEntity saved = orders.saveAndFlush(order);
                if (first == null) {
                    first = saved.getId();
                }
            }
            return first;
        });
    }
}
