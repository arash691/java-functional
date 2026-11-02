package shopflow.w06;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** SF-01 acceptance tests. */
@Tag("w06")
@DisplayName("SF-01 Inventory")
class InventoryTest {

    private final Product mug = new Product("M-1", "Mug", "mugs", 800);
    private final Product tee = new Product("T-1", "tee", "shirts", 1500);
    private final Product cap = new Product("C-1", "Cap", "hats", 800);
    private Inventory inv;

    @BeforeEach
    void setUp() {
        inv = new Inventory();
        inv.register(mug, 10);
        inv.register(tee, 2);
        inv.register(cap, 2);
    }

    @Nested
    @DisplayName("stock levels")
    class Stock {
        @Test
        void stockOfKnownAndUnknownSkus() {
            assertThat(inv.stockOf("M-1")).isEqualTo(10);
            assertThat(inv.stockOf("nope")).isZero();
        }

        @Test
        void addIncreasesStock() {
            inv.add("M-1", 5);
            assertThat(inv.stockOf("M-1")).isEqualTo(15);
        }

        @Test
        void removeDecreasesStock() {
            inv.remove("M-1", 4);
            assertThat(inv.stockOf("M-1")).isEqualTo(6);
        }

        @Test
        void removeMoreThanAvailableFailsAndKeepsTheStock() {
            assertThatThrownBy(() -> inv.remove("T-1", 3))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Insufficient stock for T-1: have 2, need 3");
            assertThat(inv.stockOf("T-1")).isEqualTo(2);
        }

        @Test
        void invalidArgumentsAreRejected() {
            assertThatThrownBy(() -> inv.add("nope", 1)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> inv.add("M-1", 0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> inv.remove("nope", 1)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> inv.remove("M-1", -1)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void registerRejectsDuplicatesAndNegativeStock() {
            assertThatThrownBy(() -> inv.register(mug, 1)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> inv.register(new Product("X", "x", "x", 1), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("low stock")
    class LowStock {
        @Test
        void listsEntriesAtOrBelowThresholdLowestFirstThenBySku() {
            assertThat(inv.lowStock(2)).containsExactly(new StockEntry("C-1", 2), new StockEntry("T-1", 2));
        }

        @Test
        void thresholdIsInclusiveAndTracksChanges() {
            inv.remove("M-1", 9);
            assertThat(inv.lowStock(2)).containsExactly(
                    new StockEntry("M-1", 1), new StockEntry("C-1", 2), new StockEntry("T-1", 2));
        }

        @Test
        void emptyWhenEverythingIsAboveThreshold() {
            assertThat(inv.lowStock(0)).isEmpty();
        }
    }

    @Nested
    @DisplayName("sorted views")
    class Sorted {
        @Test
        void byPriceCheapestFirstTiesBySku() {
            assertThat(inv.productsSortedByPrice()).containsExactly(cap, mug, tee);
        }

        @Test
        void byNameIgnoringCaseThenPrice() {
            assertThat(inv.productsSortedByNameThenPrice()).containsExactly(cap, mug, tee);
        }
    }

    @Test
    @DisplayName("callers cannot change the inventory through skus()")
    void skusIsAReadOnlyView() {
        assertThat(inv.skus()).containsExactlyInAnyOrder("M-1", "T-1", "C-1");
        assertThatThrownBy(() -> inv.skus().add("HACK")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> inv.skus().remove("M-1")).isInstanceOf(UnsupportedOperationException.class);
        assertThat(inv.skus()).hasSize(3);
    }
}
