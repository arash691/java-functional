package shopflow.w04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("w04")
@DisplayName("EX-04-01 Customer")
class CustomerTest {

    @Test
    void exposesNameAndEmail() {
        Customer c = new Customer("Sara Ahmadi", "sara@example.com");
        assertThat(c.name()).isEqualTo("Sara Ahmadi");
        assertThat(c.email()).isEqualTo("sara@example.com");
    }

    @Test
    void displayNameCombinesBoth() {
        assertThat(new Customer("Sara Ahmadi", "sara@example.com").displayName())
                .isEqualTo("Sara Ahmadi <sara@example.com>");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void rejectsBlankName(String name) {
        assertThatThrownBy(() -> new Customer(name, "sara@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"no-at-sign", "@nolocal", "nodomain@", "two@@signs.com", "a@b@c.com"})
    void rejectsBadEmail(String email) {
        assertThatThrownBy(() -> new Customer("Sara", email))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withEmailReturnsANewCustomerAndLeavesTheOriginalAlone() {
        Customer original = new Customer("Sara", "old@example.com");
        Customer changed = original.withEmail("new@example.com");
        assertThat(changed).isNotSameAs(original);
        assertThat(changed.email()).isEqualTo("new@example.com");
        assertThat(changed.name()).isEqualTo("Sara");
        assertThat(original.email()).as("original must not change").isEqualTo("old@example.com");
    }

    @Test
    void withEmailValidatesToo() {
        Customer original = new Customer("Sara", "old@example.com");
        assertThatThrownBy(() -> original.withEmail("broken")).isInstanceOf(IllegalArgumentException.class);
    }
}
