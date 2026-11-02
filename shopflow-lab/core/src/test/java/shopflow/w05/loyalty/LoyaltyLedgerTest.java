package shopflow.w05.loyalty;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w05")
@DisplayName("INC-05-1 Loyalty points vanish after a customer is renamed")
class LoyaltyLedgerTest {

    @Test
    @DisplayName("GUARD: points are found for an unchanged customer")
    void pointsAreFoundBeforeAnyRename() {
        LoyaltyLedger ledger = new LoyaltyLedger();
        Customer sara = new Customer("C-1", "Sara");
        ledger.add(sara, 120);
        assertThat(ledger.pointsOf(sara)).isEqualTo(120);
    }

    @Test
    @DisplayName("GUARD: unknown customers have zero points and customers stay separate")
    void unknownAndSeparateCustomers() {
        LoyaltyLedger ledger = new LoyaltyLedger();
        ledger.add(new Customer("C-1", "Sara"), 50);
        assertThat(ledger.pointsOf(new Customer("C-2", "Omid"))).isZero();
        assertThat(ledger.customerCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("points are still there after the customer is renamed")
    void pointsSurviveRename() {
        LoyaltyLedger ledger = new LoyaltyLedger();
        Customer sara = new Customer("C-1", "Sara");
        ledger.add(sara, 120);

        sara.rename("Sara Ahmadi");

        assertThat(ledger.pointsOf(sara)).as("points after rename").isEqualTo(120);
    }

    @Test
    @DisplayName("points earned after a rename add to the same account")
    void pointsAfterRenameAccumulate() {
        LoyaltyLedger ledger = new LoyaltyLedger();
        Customer sara = new Customer("C-1", "Sara");
        ledger.add(sara, 100);
        sara.rename("Sara Ahmadi");
        ledger.add(sara, 20);

        assertThat(ledger.customerCount()).as("number of accounts").isEqualTo(1);
        assertThat(ledger.pointsOf(sara)).as("points").isEqualTo(120);
    }

    @Test
    @DisplayName("CLUE: the total is intact, so the points exist but cannot be found")
    void totalIsStillThere() {
        LoyaltyLedger ledger = new LoyaltyLedger();
        Customer sara = new Customer("C-1", "Sara");
        ledger.add(sara, 120);
        sara.rename("Sara Ahmadi");
        assertThat(ledger.totalPoints()).isEqualTo(120);
    }
}
