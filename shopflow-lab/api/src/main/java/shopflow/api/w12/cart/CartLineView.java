package shopflow.api.w12.cart;

import java.math.BigDecimal;

public record CartLineView(String sku, String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
}
