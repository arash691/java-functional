package shopflow.api.w12.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartView(String customerId, List<CartLineView> lines, BigDecimal total) {
}
