package shopflow.w06.promo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w06")
@DisplayName("INC-06-1 The nightly promotion cleanup job crashes")
class PromotionCleanupTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);

    private static Promotion active(String code) {
        return new Promotion(code, TODAY.plusDays(5));
    }

    private static Promotion expired(String code) {
        return new Promotion(code, TODAY.minusDays(1));
    }

    private final PromotionCleanup cleanup = new PromotionCleanup();

    @Test
    @DisplayName("GUARD: nothing expired means nothing removed")
    void nothingExpired() {
        List<Promotion> promos = new ArrayList<>(List.of(active("A"), active("B")));
        assertThat(cleanup.removeExpired(promos, TODAY)).isZero();
        assertThat(promos).extracting(Promotion::code).containsExactly("A", "B");
    }

    @Test
    @DisplayName("GUARD: a promotion that expires today is still valid today")
    void expiresTodayIsStillValid() {
        List<Promotion> promos = new ArrayList<>(List.of(new Promotion("T", TODAY)));
        assertThat(cleanup.removeExpired(promos, TODAY)).isZero();
    }

    @Test
    @DisplayName("removes expired promotions from the middle of the list")
    void removesFromTheMiddle() {
        List<Promotion> promos = new ArrayList<>(
                List.of(active("A"), expired("B"), active("C"), expired("D"), active("E")));

        int removed = cleanup.removeExpired(promos, TODAY);

        assertThat(removed).isEqualTo(2);
        assertThat(promos).extracting(Promotion::code).containsExactly("A", "C", "E");
    }

    @Test
    @DisplayName("removes everything when everything has expired")
    void removesAll() {
        List<Promotion> promos = new ArrayList<>(List.of(expired("A"), expired("B"), expired("C")));
        assertThat(cleanup.removeExpired(promos, TODAY)).isEqualTo(3);
        assertThat(promos).isEmpty();
    }

    @Test
    @DisplayName("also removes the LAST expired item when the one before it is expired too")
    void secondToLastQuirk() {
        List<Promotion> promos = new ArrayList<>(List.of(active("A"), expired("B"), expired("C")));

        int removed = cleanup.removeExpired(promos, TODAY);

        assertThat(promos).as("promotions left").extracting(Promotion::code).containsExactly("A");
        assertThat(removed).isEqualTo(2);
    }
}
