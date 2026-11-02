package shopflow.w04.incident;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w04")
@DisplayName("INC-04-01 Cart items duplicate and vanish (RL-04)")
class CartIncidentTest {

    @Test
    @DisplayName("GUARD: different SKUs are different products")
    void differentSkusAreDifferent() {
        Cart cart = new Cart();
        cart.add(new ProductListing("ABC-0001", "Mug"));
        cart.add(new ProductListing("ABC-0002", "Plate"));
        assertThat(cart.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("GUARD: adding the very same object twice keeps one entry")
    void sameInstanceTwice() {
        Cart cart = new Cart();
        ProductListing mug = new ProductListing("ABC-0001", "Mug");
        cart.add(mug);
        assertThat(cart.add(mug)).isFalse();
        assertThat(cart.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("GUARD: equals itself is symmetric and SKU based")
    void equalsIsSkuBased() {
        ProductListing a = new ProductListing("ABC-0001", "Mug");
        ProductListing b = new ProductListing("ABC-0001", "Mug (new photo)");
        assertThat(a.equals(b)).isTrue();
        assertThat(b.equals(a)).isTrue();
        assertThat(a.equals(new ProductListing("ABC-0002", "Mug"))).isFalse();
    }

    @Test
    @DisplayName("REPRO: the same product loaded twice must appear once")
    void sameProductLoadedTwice() {
        Cart cart = new Cart();
        cart.add(new ProductListing("ABC-0001", "Mug"));
        boolean added = cart.add(new ProductListing("ABC-0001", "Mug"));
        assertThat(added).as("second add of an equal product").isFalse();
        assertThat(cart.size()).as("cart size").isEqualTo(1);
    }

    @Test
    @DisplayName("REPRO: an equal product can be found")
    void containsFindsEqualProduct() {
        Cart cart = new Cart();
        cart.add(new ProductListing("ABC-0001", "Mug"));
        assertThat(cart.contains(new ProductListing("ABC-0001", "Mug"))).isTrue();
    }

    @Test
    @DisplayName("REPRO: an equal product can be removed")
    void removeWorksForEqualProduct() {
        Cart cart = new Cart();
        cart.add(new ProductListing("ABC-0001", "Mug"));
        assertThat(cart.remove(new ProductListing("ABC-0001", "Mug"))).isTrue();
        assertThat(cart.size()).isZero();
    }
}
