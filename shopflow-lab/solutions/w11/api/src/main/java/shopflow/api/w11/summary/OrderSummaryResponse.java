package shopflow.api.w11.summary;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Convention (ADR-004): instants are ISO-8601 with a zone ("...Z"); money is a string so no client ever rounds it. */
public record OrderSummaryResponse(
        Long id,
        Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal total) {
}
