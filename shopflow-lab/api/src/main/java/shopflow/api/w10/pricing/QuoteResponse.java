package shopflow.api.w10.pricing;

import java.math.BigDecimal;

public record QuoteResponse(BigDecimal subtotal, BigDecimal tax, BigDecimal shipping, BigDecimal total) {
}
