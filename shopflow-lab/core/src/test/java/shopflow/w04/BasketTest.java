package shopflow.w04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w04")
@DisplayName("EX-04-04 Basket defensive copy")
class BasketTest {

    @Test
    void containsWhatWasPassedIn() {
        Basket basket = new Basket(List.of("ABC-0001", "ABC-0002"));
        assertThat(basket.skus()).containsExactly("ABC-0001", "ABC-0002");
        assertThat(basket.size()).isEqualTo(2);
    }

    @Test
    void changingTheOriginalListLaterDoesNotChangeTheBasket() {
        List<String> original = new ArrayList<>(List.of("ABC-0001"));
        Basket basket = new Basket(original);
        original.add("ABC-0002");
        assertThat(basket.size()).as("basket must have its own copy").isEqualTo(1);
    }

    @Test
    void theListReturnedByTheBasketCannotBeUsedToChangeIt() {
        Basket basket = new Basket(new ArrayList<>(List.of("ABC-0001")));
        assertThatThrownBy(() -> basket.skus().add("ABC-0002"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void nullListIsRejected() {
        assertThatThrownBy(() -> new Basket(null)).isInstanceOf(NullPointerException.class);
    }
}
