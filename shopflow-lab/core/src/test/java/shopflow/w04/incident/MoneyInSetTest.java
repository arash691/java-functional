package shopflow.w04.incident;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import shopflow.w04.Money;

/**
 * INC-04-01 follow-up: your own Money class, from session 2, has the same disease.
 * Do this AFTER you have fixed ProductListing.
 */
@Tag("w04")
@DisplayName("INC-04-01 follow-up: Money as a set element / map key")
class MoneyInSetTest {

    @Test
    void equalMoneyCollapsesInASet() {
        Set<Money> prices = new HashSet<>();
        prices.add(Money.of("1.5", "EUR"));
        prices.add(Money.of("1.50", "EUR"));
        assertThat(prices).hasSize(1);
    }

    @Test
    void equalMoneyIsFoundAsAMapKey() {
        Map<Money, String> labels = new HashMap<>();
        labels.put(Money.of("9.99", "EUR"), "special");
        assertThat(labels.get(Money.of("9.99", "EUR"))).isEqualTo("special");
    }

    @Test
    void equalObjectsHaveEqualHashCodes() {
        assertThat(Money.of("2.00", "USD").hashCode()).isEqualTo(Money.of("2", "USD").hashCode());
    }
}
