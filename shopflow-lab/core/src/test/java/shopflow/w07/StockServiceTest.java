package shopflow.w07;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w07")
@DisplayName("EX-07-01 Custom exceptions and translation")
class StockServiceTest {

    @Nested
    @DisplayName("the exception classes")
    class ExceptionClasses {
        @Test
        void outOfStockCarriesDataAndMessage() {
            OutOfStockException e = new OutOfStockException("SKU-1", 5, 2);
            assertThat(e).isInstanceOf(InventoryException.class);
            assertThat(e.sku()).isEqualTo("SKU-1");
            assertThat(e.requested()).isEqualTo(5);
            assertThat(e.available()).isEqualTo(2);
            assertThat(e).hasMessage("Out of stock: SKU-1 (requested 5, available 2)");
        }

        @Test
        void unknownSkuCarriesDataAndMessage() {
            UnknownSkuException e = new UnknownSkuException("X-9");
            assertThat(e).isInstanceOf(InventoryException.class);
            assertThat(e.sku()).isEqualTo("X-9");
            assertThat(e).hasMessage("Unknown SKU: X-9");
        }
    }

    @Nested
    @DisplayName("StockService.available")
    class Available {
        @Test
        void returnsTheGatewayQuantity() {
            StockService service = new StockService(sku -> 7);
            assertThat(service.available("SKU-1")).isEqualTo(7);
        }

        @Test
        void minusOneMeansUnknownSku() {
            StockService service = new StockService(sku -> -1);
            assertThatThrownBy(() -> service.available("X-9"))
                    .isInstanceOf(UnknownSkuException.class)
                    .hasMessage("Unknown SKU: X-9");
        }

        @Test
        @DisplayName("a GatewayException becomes an InventoryException that keeps the original as its cause")
        void translatesAndKeepsTheCause() {
            GatewayException down = new GatewayException("warehouse timeout");
            StockService service = new StockService(sku -> {
                throw down;
            });
            assertThatThrownBy(() -> service.available("SKU-1"))
                    .isInstanceOf(InventoryException.class)
                    .hasMessage("Could not read stock for SKU-1")
                    .hasCause(down);
        }
    }

    @Nested
    @DisplayName("StockService.require")
    class Require {
        @Test
        void passesWhenEnoughStock() {
            StockService service = new StockService(sku -> 5);
            service.require("SKU-1", 5);
        }

        @Test
        void throwsOutOfStockWithTheNumbers() {
            StockService service = new StockService(sku -> 2);
            assertThatThrownBy(() -> service.require("SKU-1", 5))
                    .isInstanceOfSatisfying(OutOfStockException.class, e -> {
                        assertThat(e.requested()).isEqualTo(5);
                        assertThat(e.available()).isEqualTo(2);
                    });
        }

        @Test
        void unknownSkuPropagates() {
            StockService service = new StockService(sku -> -1);
            assertThatThrownBy(() -> service.require("X", 1)).isInstanceOf(UnknownSkuException.class);
        }
    }
}
