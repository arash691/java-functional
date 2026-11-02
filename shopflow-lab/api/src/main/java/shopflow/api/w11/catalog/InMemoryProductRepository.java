package shopflow.api.w11.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

/** Week 11 has no database yet: products live in memory. (Week 12 replaces this with Postgres.) */
@Repository
public class InMemoryProductRepository {

    private final Map<Long, Product> products = new ConcurrentSkipListMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public InMemoryProductRepository() {
        resetToSeed();
    }

    public synchronized void resetToSeed() {
        products.clear();
        sequence.set(0);
        save(new Product(null, "SKU-1", "Notebook", new BigDecimal("10.00")));
        save(new Product(null, "SKU-2", "Pen", new BigDecimal("5.00")));
    }

    public Optional<Product> findById(long id) {
        return Optional.ofNullable(products.get(id));
    }

    public Optional<Product> findBySku(String sku) {
        return products.values().stream().filter(p -> p.sku().equals(sku)).findFirst();
    }

    public List<Product> findAll() {
        return List.copyOf(products.values());
    }

    public Product save(Product p) {
        long id = p.id() != null ? p.id() : sequence.incrementAndGet();
        Product saved = new Product(id, p.sku(), p.name(), p.price());
        products.put(id, saved);
        return saved;
    }

    public boolean deleteById(long id) {
        return products.remove(id) != null;
    }
}
