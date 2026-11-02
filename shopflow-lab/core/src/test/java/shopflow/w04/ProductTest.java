package shopflow.w04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w04")
@DisplayName("SF-03 Product")
class ProductTest {

    private static final Money TEN = Money.of("10.00", "EUR");

    @Test
    void holdsItsFields() {
        Product p = new Product("ABC-0001", "Mug", TEN, Category.HOME);
        assertThat(p.sku()).isEqualTo("ABC-0001");
        assertThat(p.name()).isEqualTo("Mug");
        assertThat(p.price()).isEqualTo(TEN);
        assertThat(p.category()).isEqualTo(Category.HOME);
    }

    @Test
    void rejectsBlankSkuOrName() {
        assertThatThrownBy(() -> new Product(" ", "Mug", TEN, Category.HOME)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Product("ABC-0001", null, TEN, Category.HOME)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMissingPriceOrCategory() {
        assertThatThrownBy(() -> new Product("ABC-0001", "Mug", null, Category.HOME)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Product("ABC-0001", "Mug", TEN, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void priceMustBePositive() {
        assertThatThrownBy(() -> new Product("ABC-0001", "Mug", Money.zero("EUR"), Category.HOME))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Product("ABC-0001", "Mug", Money.of("-1.00", "EUR"), Category.HOME))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withPriceReturnsACopy() {
        Product p = new Product("ABC-0001", "Mug", TEN, Category.HOME);
        Product cheaper = p.withPrice(Money.of("8.50", "EUR"));
        assertThat(cheaper.price()).isEqualTo(Money.of("8.50", "EUR"));
        assertThat(cheaper.sku()).isEqualTo("ABC-0001");
        assertThat(p.price()).as("original unchanged").isEqualTo(TEN);
    }

    @Test
    void withPriceValidates() {
        Product p = new Product("ABC-0001", "Mug", TEN, Category.HOME);
        assertThatThrownBy(() -> p.withPrice(Money.zero("EUR"))).isInstanceOf(IllegalArgumentException.class);
    }
}
