package shopflow.api.w10.pricing;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

public record QuoteRequest(@NotEmpty List<@Valid Line> items, @NotBlank String country) {

    public record Line(@NotBlank String sku, @Positive int quantity) {
    }
}
