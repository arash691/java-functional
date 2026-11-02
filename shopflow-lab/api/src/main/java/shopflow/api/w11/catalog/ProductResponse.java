package shopflow.api.w11.catalog;

import java.math.BigDecimal;

public record ProductResponse(Long id, String sku, String name, BigDecimal price) {

    static ProductResponse from(Product p) {
        return new ProductResponse(p.id(), p.sku(), p.name(), p.price());
    }
}
