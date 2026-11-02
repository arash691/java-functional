package shopflow.api.w12.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class ProductCatalog {

    private final ProductRepository repository;

    public ProductCatalog(ProductRepository repository) {
        this.repository = repository;
    }

    public Optional<ProductView> findBySku(String sku) {
        return repository.findBySku(sku).map(ProductCatalog::toView);
    }

    public List<ProductView> searchByName(String fragment) {
        return repository.findByNameContainingIgnoreCaseOrderByNameAsc(fragment).stream().map(ProductCatalog::toView).toList();
    }

    public ProductView create(String sku, String name, String description, BigDecimal price) {
        return toView(repository.save(new ProductEntity(sku, name, description, price)));
    }

    private static ProductView toView(ProductEntity p) {
        return new ProductView(p.getId(), p.getSku(), p.getName(), p.getDescription(), p.getPrice());
    }
}
