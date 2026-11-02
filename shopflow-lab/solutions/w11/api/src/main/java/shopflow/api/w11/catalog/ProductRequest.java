package shopflow.api.w11.catalog;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Pattern(regexp = "[A-Z0-9-]{3,20}", message = "must be 3-20 characters of A-Z, 0-9 or '-'") String sku,
        @NotBlank @Size(max = 100) String name,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price) {
}
