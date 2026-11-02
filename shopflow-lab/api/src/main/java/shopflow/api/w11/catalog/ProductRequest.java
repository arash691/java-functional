package shopflow.api.w11.catalog;

import java.math.BigDecimal;

/**
 * Body of POST /api/products and PUT /api/products/{id}.
 * EX-11-03: sku = 3-20 chars of A-Z, 0-9, '-' ; name = not blank, max 100 ; price = positive, at most 2 decimals, max 8 integer digits.
 */
public record ProductRequest(String sku, String name, BigDecimal price) {
}
