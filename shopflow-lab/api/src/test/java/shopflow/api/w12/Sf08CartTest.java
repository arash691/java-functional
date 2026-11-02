package shopflow.api.w12;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import shopflow.api.w12.cart.CartService;
import shopflow.api.w12.cart.UnknownProductException;
import shopflow.api.w12.catalog.ProductEntity;
import shopflow.api.w12.catalog.ProductRepository;

/**
 * SF-08: carts live in Postgres. Database contract the tests rely on:
 * table cart (one row per customer) and table cart_item (one row per SKU in a cart).
 */
@Tag("w12")
@DisplayName("SF-08 persistent cart")
class Sf08CartTest extends AbstractPostgresTest {

    @Autowired
    CartService carts;

    @Autowired
    ProductRepository products;

    @BeforeEach
    void seed() {
        products.save(new ProductEntity("SKU-1", "Notebook", new BigDecimal("10.00")));
        products.save(new ProductEntity("SKU-2", "Pen", new BigDecimal("5.00")));
    }

    @Test
    @DisplayName("an unknown customer has an empty cart with total 0.00")
    void emptyCart() {
        var cart = carts.getCart("nobody");
        assertThat(cart.customerId()).isEqualTo("nobody");
        assertThat(cart.lines()).isEmpty();
        assertThat(cart.total()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("added items appear as lines with unit price, line total and cart total")
    void addItems() {
        carts.addItem("alice", "SKU-1", 2);
        carts.addItem("alice", "SKU-2", 1);

        var cart = carts.getCart("alice");
        assertThat(cart.lines()).hasSize(2);
        var notebook = cart.lines().stream().filter(l -> l.sku().equals("SKU-1")).findFirst().orElseThrow();
        assertThat(notebook.name()).isEqualTo("Notebook");
        assertThat(notebook.quantity()).isEqualTo(2);
        assertThat(notebook.unitPrice()).isEqualByComparingTo("10.00");
        assertThat(notebook.lineTotal()).isEqualByComparingTo("20.00");
        assertThat(cart.total()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("adding the same SKU again adds to the quantity: still one line, one cart_item row")
    void sameSkuMerges() {
        carts.addItem("alice", "SKU-1", 2);
        carts.addItem("alice", "SKU-1", 3);

        assertThat(carts.getCart("alice").lines()).singleElement().extracting("quantity").isEqualTo(5);
        assertThat(count("cart")).as("rows in cart").isEqualTo(1);
        assertThat(count("cart_item")).as("rows in cart_item").isEqualTo(1);
    }

    @Test
    @DisplayName("carts of different customers are separate")
    void separateCarts() {
        carts.addItem("alice", "SKU-1", 1);
        carts.addItem("bob", "SKU-2", 4);

        assertThat(carts.getCart("alice").total()).isEqualByComparingTo("10.00");
        assertThat(carts.getCart("bob").total()).isEqualByComparingTo("20.00");
        assertThat(count("cart")).isEqualTo(2);
    }

    @Test
    @DisplayName("unknown SKU -> UnknownProductException and nothing stored")
    void unknownSku() {
        assertThatThrownBy(() -> carts.addItem("alice", "NOPE", 1)).isInstanceOf(UnknownProductException.class);
        assertThat(count("cart_item")).isZero();
    }

    @Test
    @DisplayName("quantity must be positive")
    void quantityMustBePositive() {
        assertThatThrownBy(() -> carts.addItem("alice", "SKU-1", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> carts.addItem("alice", "SKU-1", -2)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("clear empties the cart")
    void clear() {
        carts.addItem("alice", "SKU-1", 1);
        carts.clear("alice");
        assertThat(carts.getCart("alice").lines()).isEmpty();
        assertThat(count("cart_item")).isZero();
    }
}
