package shopflow.w04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("w04")
@DisplayName("EX-04-03 Address record")
class AddressTest {

    @Test
    void recordsGiveYouEqualityForFree() {
        assertThat(new Address("Main St 1", "Berlin", "DE")).isEqualTo(new Address("Main St 1", "Berlin", "DE"));
    }

    @Test
    void recordsGiveYouToString() {
        assertThat(new Address("Main St 1", "Berlin", "DE").toString()).contains("Berlin").contains("DE");
    }

    @ParameterizedTest
    @ValueSource(strings = {"de", "D", "DEU", "D1", ""})
    void rejectsBadCountryCodes(String code) {
        assertThatThrownBy(() -> new Address("Main St 1", "Berlin", code))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankStreetAndCity() {
        assertThatThrownBy(() -> new Address(" ", "Berlin", "DE")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Address("Main St 1", "", "DE")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withCityReturnsACopyWithOnlyTheCityChanged() {
        Address a = new Address("Main St 1", "Berlin", "DE");
        Address b = a.withCity("Hamburg");
        assertThat(b).isEqualTo(new Address("Main St 1", "Hamburg", "DE"));
        assertThat(a.city()).as("original unchanged").isEqualTo("Berlin");
    }

    @Test
    void withCityValidates() {
        Address a = new Address("Main St 1", "Berlin", "DE");
        assertThatThrownBy(() -> a.withCity(" ")).isInstanceOf(IllegalArgumentException.class);
    }
}
