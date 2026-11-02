package shopflow.w05;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag("w05")
@DisplayName("EX-05-01 OrderStatus: enum with behaviour")
class OrderStatusTest {

    @ParameterizedTest(name = "{0} -> {1} is allowed")
    @CsvSource({"CREATED,PAID", "CREATED,CANCELLED", "PAID,SHIPPED", "PAID,CANCELLED", "SHIPPED,DELIVERED"})
    void allowedTransitions(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).as("%s -> %s", from, to).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} is forbidden")
    @CsvSource({
        "CREATED,SHIPPED", "CREATED,DELIVERED", "PAID,CREATED", "PAID,DELIVERED", "SHIPPED,CANCELLED",
        "SHIPPED,PAID", "DELIVERED,CANCELLED", "CANCELLED,PAID", "DELIVERED,CREATED"
    })
    void forbiddenTransitions(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).as("%s -> %s", from, to).isFalse();
    }

    @Test
    @DisplayName("no status can transition to itself")
    void noSelfTransitions() {
        for (OrderStatus s : OrderStatus.values()) {
            assertThat(s.canTransitionTo(s)).as("%s -> %s", s, s).isFalse();
        }
    }

    @Test
    @DisplayName("DELIVERED and CANCELLED are terminal, the others are not")
    void terminalStates() {
        assertThat(Arrays.stream(OrderStatus.values()).filter(OrderStatus::isTerminal))
                .containsExactlyInAnyOrder(OrderStatus.DELIVERED, OrderStatus.CANCELLED);
    }

    @Test
    void allowedNextListsTheLegalTargets() {
        assertThat(OrderStatus.CREATED.allowedNext())
                .containsExactlyInAnyOrder(OrderStatus.PAID, OrderStatus.CANCELLED);
        assertThat(OrderStatus.PAID.allowedNext())
                .containsExactlyInAnyOrder(OrderStatus.SHIPPED, OrderStatus.CANCELLED);
        assertThat(OrderStatus.SHIPPED.allowedNext()).containsExactly(OrderStatus.DELIVERED);
        assertThat(OrderStatus.DELIVERED.allowedNext()).isEmpty();
        assertThat(OrderStatus.CANCELLED.allowedNext()).isEmpty();
    }

    @Test
    @DisplayName("allowedNext() and canTransitionTo() always agree")
    void consistent() {
        for (OrderStatus from : OrderStatus.values()) {
            for (OrderStatus to : OrderStatus.values()) {
                assertThat(from.allowedNext().contains(to)).as("%s -> %s", from, to)
                        .isEqualTo(from.canTransitionTo(to));
            }
        }
    }
}
