package shopflow.w05;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import shopflow.w05.PaymentResult.Approved;
import shopflow.w05.PaymentResult.Declined;
import shopflow.w05.PaymentResult.Pending;

@Tag("w05")
@DisplayName("EX-05-02 PaymentResult: sealed interface + pattern-matching switch")
class PaymentMessagesTest {

    @Test
    void describesApproved() {
        assertThat(PaymentMessages.describe(new Approved("T-1"))).isEqualTo("Payment approved (tx T-1)");
    }

    @Test
    void describesDeclined() {
        assertThat(PaymentMessages.describe(new Declined("insufficient funds")))
                .isEqualTo("Payment declined: insufficient funds");
    }

    @Test
    void describesPendingInSeconds() {
        assertThat(PaymentMessages.describe(new Pending(Duration.ofSeconds(90))))
                .isEqualTo("Payment pending, retry in 90 seconds");
        assertThat(PaymentMessages.describe(new Pending(Duration.ofMinutes(2))))
                .isEqualTo("Payment pending, retry in 120 seconds");
    }

    @Test
    void approvedAndDeclinedAreFinal() {
        assertThat(PaymentMessages.isFinal(new Approved("T-1"))).isTrue();
        assertThat(PaymentMessages.isFinal(new Declined("no"))).isTrue();
    }

    @Test
    void pendingIsNotFinal() {
        assertThat(PaymentMessages.isFinal(new Pending(Duration.ofSeconds(5)))).isFalse();
    }
}
