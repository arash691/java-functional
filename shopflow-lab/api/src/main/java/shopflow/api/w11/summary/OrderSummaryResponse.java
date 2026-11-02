package shopflow.api.w11.summary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(Long id, LocalDateTime createdAt, BigDecimal total) {
}
