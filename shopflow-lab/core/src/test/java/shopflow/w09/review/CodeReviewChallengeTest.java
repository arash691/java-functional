package shopflow.w09.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Gate prep: eight problems are hiding in the production code of this package.
 * Do a read-only review first, then use these tests to check how many you found.
 */
@Tag("w09")
class CodeReviewChallengeTest {

    private static final Sku A = new Sku("A");
    private static final Sku B = new Sku("B");

    private final Checkout checkout = new Checkout(Clock.fixed(Instant.parse("2025-03-09T07:00:00Z"), ZoneOffset.UTC));

    @Nested
    @DisplayName("Money")
    class Money {
        @Test
        @DisplayName("guard: a single price is exact")
        void singleItem() {
            Cart cart = new Cart();
            cart.add(A, 1);
            assertThat(checkout.total(cart, Map.of(A, new BigDecimal("0.50")))).isEqualByComparingTo("0.50");
        }

        @Test
        @DisplayName("ISSUE: three ten-cent items cost exactly thirty cents")
        void threeTenCentItemsAreExactlyThirtyCents() {
            Cart cart = new Cart();
            cart.add(A, 1);
            cart.add(B, 1);
            cart.add(A, 1);
            Map<Sku, BigDecimal> prices = Map.of(A, new BigDecimal("0.10"), B, new BigDecimal("0.10"));

            assertThat(checkout.total(cart, prices)).isEqualByComparingTo("0.30");
        }
    }

    @Nested
    @DisplayName("Time")
    class Time {
        @Test
        @DisplayName("ISSUE: the receipt is stamped by the injected clock")
        void receiptUsesTheClock() {
            Cart cart = new Cart();
            cart.add(A, 1);

            Checkout.Receipt receipt = checkout.receipt("O-1", cart, Map.of(A, new BigDecimal("1.00")));

            assertThat(receipt.issuedAt()).isEqualTo(Instant.parse("2025-03-09T07:00:00Z"));
        }
    }

    @Nested
    @DisplayName("Errors")
    class Errors {
        @TempDir
        Path dir;

        @Test
        @DisplayName("guard: a valid price list loads")
        void validFile() throws IOException {
            Path file = dir.resolve("prices.csv");
            Files.writeString(file, "A,0.10\nB,2.50\n");

            assertThat(checkout.loadPriceList(file).values())
                    .containsExactlyInAnyOrder(new BigDecimal("0.10"), new BigDecimal("2.50"));
        }

        @Test
        @DisplayName("ISSUE: a missing price list is reported, not silently ignored")
        void missingFile() {
            Path missing = dir.resolve("nope.csv");

            assertThatThrownBy(() -> checkout.loadPriceList(missing))
                    .isInstanceOf(PriceListException.class)
                    .hasCauseInstanceOf(NoSuchFileException.class);
        }

        @Test
        @DisplayName("ISSUE: a malformed line is reported with its cause")
        void malformedLine() throws IOException {
            Path file = dir.resolve("bad.csv");
            Files.writeString(file, "A,0.10\nB,not-a-number\n");

            assertThatThrownBy(() -> checkout.loadPriceList(file))
                    .isInstanceOf(PriceListException.class)
                    .hasCauseInstanceOf(NumberFormatException.class);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {
        @Test
        @DisplayName("ISSUE: equal SKUs are found in a hash set")
        void equalSkusHashAlike() {
            Set<Sku> skus = new HashSet<>();
            skus.add(new Sku("SKU-1"));

            assertThat(skus).contains(new Sku("SKU-1"));
        }

        @Test
        @DisplayName("ISSUE: loyalty points survive a rename")
        void pointsSurviveRename() {
            LoyaltyPoints loyalty = new LoyaltyPoints();
            Customer sara = new Customer("C-1", "Sara");
            loyalty.add(sara, 50);

            sara.rename("Sara B.");

            assertThat(loyalty.pointsOf(sara)).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Collections")
    class Collections {
        @Test
        @DisplayName("ISSUE: a snapshot of the lines does not change when the cart changes")
        void linesAreACopy() {
            Cart cart = new Cart();
            cart.add(A, 1);
            cart.add(B, 2);
            List<Cart.Line> snapshot = cart.lines();

            cart.add(A, 3);

            assertThat(snapshot).hasSize(2);
        }

        @Test
        @DisplayName("ISSUE: empty lines can be removed")
        void removeEmptyLines() {
            Cart cart = new Cart();
            cart.add(A, 1);
            cart.add(B, 0);
            cart.add(new Sku("C"), 2);
            cart.add(new Sku("D"), 3);

            cart.removeEmptyLines();

            assertThat(cart.lines()).extracting(Cart.Line::quantity).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("guard: small quantities compare equal")
        void smallQuantities() {
            Cart cart = new Cart();
            assertThat(cart.sameQuantity(new Cart.Line(A, 5), new Cart.Line(B, 5))).isTrue();
        }

        @Test
        @DisplayName("ISSUE: large equal quantities compare equal too")
        void largeQuantities() {
            Cart cart = new Cart();
            assertThat(cart.sameQuantity(new Cart.Line(A, 1000), new Cart.Line(B, 1000))).isTrue();
        }
    }
}
