package shopflow.api.w12.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * The catalog use cases, working on views (never on entities).
 * EX-12-01: findBySku, searchByName.   EX-12-02: create (with description) and description in the view.
 */
@Service
public class ProductCatalog {

    private final ProductRepository repository;

    public ProductCatalog(ProductRepository repository) {
        this.repository = repository;
    }

    /** Exact SKU match. */
    public Optional<ProductView> findBySku(String sku) {
        throw new UnsupportedOperationException("TODO EX-12-01");
    }

    /** Case-insensitive "contains" on the name, ordered by name ascending. */
    public List<ProductView> searchByName(String fragment) {
        throw new UnsupportedOperationException("TODO EX-12-01");
    }

    /** Stores a product including its description (nullable) and returns the stored view. */
    public ProductView create(String sku, String name, String description, BigDecimal price) {
        throw new UnsupportedOperationException("TODO EX-12-02");
    }
}
