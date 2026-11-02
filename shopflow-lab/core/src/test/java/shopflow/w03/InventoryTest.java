package shopflow.w03;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w03")
@DisplayName("SF-01 Inventory core")
class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
    }

    @Nested
    @DisplayName("a new inventory")
    class Empty {
        @Test
        void hasZeroStockForUnknownSku() {
            assertThat(inventory.stockOf("ABC-0001")).isZero();
        }

        @Test
        void hasNoDistinctSkus() {
            assertThat(inventory.distinctSkus()).isZero();
        }
    }

    @Nested
    @DisplayName("add")
    class Add {
        @Test
        void recordsStock() {
            inventory.add("ABC-0001", 5);
            assertThat(inventory.stockOf("ABC-0001")).isEqualTo(5);
        }

        @Test
        void accumulatesOverMultipleCalls() {
            inventory.add("ABC-0001", 5);
            inventory.add("ABC-0001", 7);
            assertThat(inventory.stockOf("ABC-0001")).isEqualTo(12);
        }

        @Test
        void keepsSkusSeparate() {
            inventory.add("ABC-0001", 1);
            inventory.add("ABC-0002", 2);
            assertThat(inventory.stockOf("ABC-0001")).isEqualTo(1);
            assertThat(inventory.stockOf("ABC-0002")).isEqualTo(2);
            assertThat(inventory.distinctSkus()).isEqualTo(2);
        }

        @Test
        void rejectsNonPositiveQuantity() {
            assertThatThrownBy(() -> inventory.add("ABC-0001", 0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> inventory.add("ABC-0001", -3)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsNullOrBlankSku() {
            assertThatThrownBy(() -> inventory.add(null, 1)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> inventory.add("  ", 1)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("remove")
    class Remove {
        @Test
        void decreasesStock() {
            inventory.add("ABC-0001", 10);
            inventory.remove("ABC-0001", 4);
            assertThat(inventory.stockOf("ABC-0001")).isEqualTo(6);
        }

        @Test
        void canRemoveEverything_andTheSkuNoLongerCounts() {
            inventory.add("ABC-0001", 3);
            inventory.remove("ABC-0001", 3);
            assertThat(inventory.stockOf("ABC-0001")).isZero();
            assertThat(inventory.distinctSkus()).isZero();
        }

        @Test
        void cannotRemoveMoreThanAvailable_messageNamesTheSku() {
            inventory.add("ABC-0001", 2);
            assertThatThrownBy(() -> inventory.remove("ABC-0001", 3))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ABC-0001");
            assertThat(inventory.stockOf("ABC-0001")).as("a failed remove must not change stock").isEqualTo(2);
        }

        @Test
        void cannotRemoveFromUnknownSku() {
            assertThatThrownBy(() -> inventory.remove("ABC-9999", 1))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void rejectsNonPositiveQuantity() {
            inventory.add("ABC-0001", 5);
            assertThatThrownBy(() -> inventory.remove("ABC-0001", 0)).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
