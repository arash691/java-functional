package shopflow.api.w12;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import shopflow.api.w12.catalog.ProductEntity;
import shopflow.api.w12.catalog.ProductRepository;

/**
 * WORKED EXAMPLE (green from the start): entity + Spring Data repository + Flyway migration V1 + real Postgres.
 * Run it, then read ProductEntity, ProductRepository and db/migration/V1__create_product.sql side by side.
 */
@Tag("w12")
@DisplayName("Worked example: ProductRepository")
class ProductRepositoryExampleTest extends AbstractPostgresTest {

    @Autowired
    ProductRepository repository;

    @Test
    @DisplayName("save assigns an id and the row can be read back")
    void saveAndFind() {
        ProductEntity saved = repository.save(new ProductEntity("SKU-1", "Notebook", new BigDecimal("10.00")));

        assertThat(saved.getId()).isNotNull();
        ProductEntity found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getSku()).isEqualTo("SKU-1");
        assertThat(found.getPrice()).isEqualByComparingTo("10.00");
        assertThat(count("product")).isEqualTo(1);
    }
}
