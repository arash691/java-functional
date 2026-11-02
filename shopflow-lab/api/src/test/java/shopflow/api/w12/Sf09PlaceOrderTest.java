package shopflow.api.w12;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import shopflow.api.w12.cart.CartService;
import shopflow.api.w12.catalog.ProductEntity;
import shopflow.api.w12.catalog.ProductRepository;
import shopflow.api.w12.ordering.EmptyCartException;
import shopflow.api.w12.ordering.OrderService;
import shopflow.api.w12.ordering.OrderStatus;

/** SF-09: place an order from the cart. (Needs SF-08.) */
@Tag("w12")
@DisplayName("SF-09 place order")
class Sf09PlaceOrderTest extends AbstractPostgresTest {

    @Autowired
    CartService carts;

    @Autowired
    OrderService orders;

    @Autowired
    ProductRepository products;

    @BeforeEach
    void seed() {
        products.save(new ProductEntity("SKU-1", "Notebook", new BigDecimal("10.00")));
        products.save(new ProductEntity("SKU-2", "Pen", new BigDecimal("5.00")));
    }

    @Test
    @DisplayName("the order has the cart's items, status NEW, a recent createdAt and the right total")
    void placeOrder() {
        carts.addItem("alice", "SKU-1", 2);
        carts.addItem("alice", "SKU-2", 1);

        var order = orders.placeOrder("alice");

        assertThat(order.id()).isNotNull();
        assertThat(order.customerId()).isEqualTo("alice");
        assertThat(order.status()).isEqualTo(OrderStatus.NEW);
        assertThat(order.createdAt()).isBetween(Instant.now().minus(Duration.ofMinutes(1)), Instant.now().plusSeconds(5));
        assertThat(order.items()).hasSize(2);
        assertThat(order.total()).isEqualByComparingTo("25.00");
        assertThat(count("customer_order")).isEqualTo(1);
        assertThat(count("order_item")).isEqualTo(2);
    }

    @Test
    @DisplayName("the cart is empty afterwards")
    void cartIsCleared() {
        carts.addItem("alice", "SKU-1", 1);
        orders.placeOrder("alice");

        assertThat(carts.getCart("alice").lines()).isEmpty();
    }

    @Test
    @DisplayName("an empty cart -> EmptyCartException and no order is stored")
    void emptyCart() {
        assertThatThrownBy(() -> orders.placeOrder("alice")).isInstanceOf(EmptyCartException.class);
        assertThat(count("customer_order")).isZero();
    }

    @Test
    @DisplayName("unit price is a snapshot: a later price change does not alter the order")
    void priceSnapshot() {
        carts.addItem("alice", "SKU-1", 1);
        orders.placeOrder("alice");

        jdbc.update("update product set price = 99.00 where sku = 'SKU-1'");

        BigDecimal stored = jdbc.queryForObject("select unit_price from order_item", BigDecimal.class);
        assertThat(stored).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("a customer can order again with a new cart")
    void orderTwice() {
        carts.addItem("alice", "SKU-1", 1);
        orders.placeOrder("alice");
        carts.addItem("alice", "SKU-2", 3);
        var second = orders.placeOrder("alice");

        assertThat(second.total()).isEqualByComparingTo("15.00");
        assertThat(count("customer_order")).isEqualTo(2);
    }
}
