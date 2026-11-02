package shopflow.api.w12.catalog;

import java.math.BigDecimal;

public record ProductView(Long id, String sku, String name, String description, BigDecimal price) {
}
