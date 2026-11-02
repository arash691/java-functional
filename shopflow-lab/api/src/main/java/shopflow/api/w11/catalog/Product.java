package shopflow.api.w11.catalog;

import java.math.BigDecimal;

public record Product(Long id, String sku, String name, BigDecimal price) {
}
