package shopflow.api.w12;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import shopflow.api.w12.ordering.OrderQueryService;

/** INC-48: "The back-office order list takes seconds and the database CPU spikes when it opens." */
@Tag("w12")
@DisplayName("INC-48 the order list is not N+1")
class Inc48NPlusOneTest extends AbstractPostgresTest {

    @Autowired
    OrderQueryService queries;

    @Autowired
    OrderTestData data;

    @Test
    @DisplayName("GUARD: the list contains every order with all its items")
    void correctContent() {
        data.createOrders(20, 3);

        var views = queries.listWithItems();

        assertThat(views).hasSize(20);
        assertThat(views).allSatisfy(v -> assertThat(v.items()).hasSize(3));
        assertThat(views.get(0).total()).isEqualByComparingTo("33.50"); // 1x10.00 + 2x5.00 + 3x4.50
    }

    @Test
    @DisplayName("20 orders with 3 items are loaded with at most 2 SQL statements")
    void fewStatements() {
        data.createOrders(20, 3);
        statistics().clear();

        queries.listWithItems();

        long statements = statistics().getPrepareStatementCount();
        assertThat(statements).as("SQL statements for listing 20 orders (1 + N means N+1)").isLessThanOrEqualTo(2);
    }
}
