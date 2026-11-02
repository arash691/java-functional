package shopflow.api.w12.ordering;

import java.math.BigDecimal;

public record OrderItemView(String sku, String name, int quantity, BigDecimal unitPrice) {
}
