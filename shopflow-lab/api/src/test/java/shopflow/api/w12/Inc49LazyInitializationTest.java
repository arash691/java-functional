package shopflow.api.w12;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import shopflow.api.w12.ordering.OrderNotFoundException;
import shopflow.api.w12.ordering.OrderQueryService;
import shopflow.api.w12.ordering.OrderView;

/** INC-49: "Opening a single order in the back office fails with LazyInitializationException." */
@Tag("w12")
@DisplayName("INC-49 describe(id) works outside a web request")
class Inc49LazyInitializationTest extends AbstractPostgresTest {

    @Autowired
    OrderQueryService queries;

    @Autowired
    OrderTestData data;

    @Test
    @DisplayName("describe returns the order with its items")
    void describe() {
        Long id = data.createOrders(1, 3);

        assertThatCode(() -> queries.describe(id)).as("describe(%d)", id).doesNotThrowAnyException();
        OrderView view = queries.describe(id);
        assertThat(view.items()).hasSize(3);
        assertThat(view.items().get(0).sku()).isEqualTo("SKU-1");
        assertThat(view.total()).isEqualByComparingTo("33.50");
    }

    @Test
    @DisplayName("GUARD: an unknown id is OrderNotFoundException")
    void unknown() {
        assertThatThrownBy(() -> queries.describe(424242L)).isInstanceOf(OrderNotFoundException.class);
    }
}
