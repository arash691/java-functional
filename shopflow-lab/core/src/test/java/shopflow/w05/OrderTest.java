package shopflow.w05;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import shopflow.w05.Order.StatusChange;
import shopflow.w05.PaymentResult.Approved;
import shopflow.w05.PaymentResult.Declined;
import shopflow.w05.PaymentResult.Pending;

/** SF-03 acceptance tests. Needs a working OrderStatus (EX-05-01) and Order. */
@Tag("w05")
@DisplayName("SF-03 Order lifecycle")
class OrderTest {

    private Order paid() {
        Order o = new Order("O-1");
        o.applyPayment(new Approved("T-9"));
        return o;
    }

    @Nested
    @DisplayName("a new order")
    class NewOrder {
        @Test
        void startsCreatedWithNoHistory() {
            Order o = new Order("O-1");
            assertThat(o.status()).isEqualTo(OrderStatus.CREATED);
            assertThat(o.history()).isEmpty();
            assertThat(o.transactionId()).isEmpty();
            assertThat(o.failedPaymentAttempts()).isZero();
        }
    }

    @Nested
    @DisplayName("payment")
    class Payment {
        @Test
        void approvedMovesToPaidAndKeepsTransactionId() {
            Order o = paid();
            assertThat(o.status()).isEqualTo(OrderStatus.PAID);
            assertThat(o.transactionId()).contains("T-9");
            assertThat(o.history()).containsExactly(new StatusChange(OrderStatus.CREATED, OrderStatus.PAID));
        }

        @Test
        void declinedCountsTheAttemptAndStaysCreated() {
            Order o = new Order("O-1");
            o.applyPayment(new Declined("card expired"));
            o.applyPayment(new Declined("card expired"));
            assertThat(o.status()).isEqualTo(OrderStatus.CREATED);
            assertThat(o.failedPaymentAttempts()).isEqualTo(2);
            assertThat(o.history()).isEmpty();
        }

        @Test
        void pendingChangesNothing() {
            Order o = new Order("O-1");
            o.applyPayment(new Pending(Duration.ofSeconds(30)));
            assertThat(o.status()).isEqualTo(OrderStatus.CREATED);
            assertThat(o.failedPaymentAttempts()).isZero();
        }

        @Test
        void paymentsAreOnlyAcceptedWhileCreated() {
            Order o = paid();
            assertThatThrownBy(() -> o.applyPayment(new Approved("T-10"))).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> o.applyPayment(new Declined("x"))).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("shipping and delivery")
    class Fulfilment {
        @Test
        void happyPathRecordsEveryStep() {
            Order o = paid();
            o.ship();
            o.deliver();
            assertThat(o.status()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(o.history()).containsExactly(
                    new StatusChange(OrderStatus.CREATED, OrderStatus.PAID),
                    new StatusChange(OrderStatus.PAID, OrderStatus.SHIPPED),
                    new StatusChange(OrderStatus.SHIPPED, OrderStatus.DELIVERED));
        }

        @Test
        void cannotShipBeforePayment() {
            Order o = new Order("O-1");
            assertThatThrownBy(o::ship).isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot go from CREATED to SHIPPED");
        }

        @Test
        void cannotDeliverBeforeShipping() {
            Order o = paid();
            assertThatThrownBy(o::deliver).isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot go from PAID to DELIVERED");
        }
    }

    @Nested
    @DisplayName("cancellation")
    class Cancellation {
        @Test
        void allowedWhileCreatedOrPaid() {
            Order a = new Order("A");
            a.cancel();
            assertThat(a.status()).isEqualTo(OrderStatus.CANCELLED);
            Order b = paid();
            b.cancel();
            assertThat(b.status()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        void notAllowedOnceShipped() {
            Order o = paid();
            o.ship();
            assertThatThrownBy(o::cancel).isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot go from SHIPPED to CANCELLED");
        }

        @Test
        void cancelledOrderIsFinal() {
            Order o = new Order("O-1");
            o.cancel();
            assertThatThrownBy(() -> o.applyPayment(new Approved("T"))).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(o::ship).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("history cannot be modified from outside")
    void historyIsReadOnly() {
        Order o = paid();
        assertThatThrownBy(() -> o.history().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
