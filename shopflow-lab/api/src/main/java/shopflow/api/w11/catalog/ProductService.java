package shopflow.api.w11.catalog;

import java.util.List;

import org.springframework.stereotype.Service;

import shopflow.api.w11.config.CatalogProperties;

/** Business rules. Provided: this week is about the HTTP layer. */
@Service
public class ProductService {

    private final InMemoryProductRepository repository;
    private final CatalogProperties properties;

    public ProductService(InMemoryProductRepository repository, CatalogProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public ProductResponse get(long id) {
        return repository.findById(id).map(ProductResponse::from).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public ProductResponse create(ProductRequest request) {
        repository.findBySku(request.sku()).ifPresent(p -> {
            throw new DuplicateSkuException(request.sku());
        });
        return ProductResponse.from(repository.save(new Product(null, request.sku(), request.name(), request.price())));
    }

    public ProductResponse update(long id, ProductRequest request) {
        repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        repository.findBySku(request.sku()).filter(p -> !p.id().equals(id)).ifPresent(p -> {
            throw new DuplicateSkuException(request.sku());
        });
        return ProductResponse.from(repository.save(new Product(id, request.sku(), request.name(), request.price())));
    }

    public void delete(long id) {
        if (!repository.deleteById(id)) {
            throw new ProductNotFoundException(id);
        }
    }

    /** page is 0-based; size defaults to shopflow.catalog.page-size. */
    public PageResponse<ProductResponse> list(Integer page, Integer size) {
        int p = page == null ? 0 : page;
        int s = size == null ? properties.pageSize() : size;
        List<Product> all = repository.findAll();
        List<ProductResponse> items = all.stream().skip((long) p * s).limit(s).map(ProductResponse::from).toList();
        return new PageResponse<>(items, p, s, all.size());
    }
}
