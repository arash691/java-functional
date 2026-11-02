package shopflow.w05;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w05")
@DisplayName("EX-05-03 ShippingMethod: abstract class + template method")
class ShippingMethodTest {

    @Nested
    @DisplayName("quote() template method")
    class Quote {
        @Test
        void rejectsNonPositiveWeight() {
            ShippingMethod m = new PickupShipping();
            assertThatThrownBy(() -> m.quote(0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> m.quote(-5)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void formatsNameAndPrice() {
            assertThat(new PickupShipping().quote(500)).isEqualTo("Pickup: 0.00");
        }
    }

    @Nested
    @DisplayName("subclasses")
    class Subclasses {
        @Test
        void standardIsFourPlusOnePerStartedKilo() {
            ShippingMethod m = new StandardShipping();
            assertThat(m.quote(1)).isEqualTo("Standard: 5.00");
            assertThat(m.quote(1000)).isEqualTo("Standard: 5.00");
            assertThat(m.quote(1001)).isEqualTo("Standard: 6.00");
            assertThat(m.quote(2500)).isEqualTo("Standard: 7.00");
        }

        @Test
        void expressIsTwiceStandardPlusThree() {
            ShippingMethod m = new ExpressShipping();
            assertThat(m.quote(1000)).isEqualTo("Express: 13.00");
            assertThat(m.quote(2500)).isEqualTo("Express: 17.00");
        }

        @Test
        void callersUseThemPolymorphically() {
            List<ShippingMethod> methods = List.of(new StandardShipping(), new ExpressShipping(), new PickupShipping());
            assertThat(methods.stream().map(m -> m.quote(1000)))
                    .containsExactly("Standard: 5.00", "Express: 13.00", "Pickup: 0.00");
        }
    }
}
