package shopflow.api.w12;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import shopflow.api.w12.catalog.ProductCatalog;
import shopflow.api.w12.catalog.ProductEntity;
import shopflow.api.w12.catalog.ProductRepository;

/** EX-12-01: derived query methods on the repository. */
@Tag("w12")
@DisplayName("EX-12-01 find products by SKU and by name")
class Ex1ProductQueriesTest extends AbstractPostgresTest {

    @Autowired
    ProductRepository repository;

    @Autowired
    ProductCatalog catalog;

    @BeforeEach
    void seed() {
        repository.save(new ProductEntity("NB-A5", "Notebook A5", new BigDecimal("10.00")));
        repository.save(new ProductEntity("NB-A4", "Notebook A4", new BigDecimal("12.00")));
        repository.save(new ProductEntity("PEN-1", "Fountain pen", new BigDecimal("25.00")));
    }

    @Test
    @DisplayName("findBySku returns the product with that exact SKU")
    void findBySku() {
        var found = catalog.findBySku("PEN-1");
        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Fountain pen");
        assertThat(found.get().price()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("findBySku is empty for an unknown SKU")
    void findBySkuUnknown() {
        assertThat(catalog.findBySku("NOPE")).isEmpty();
    }

    @Test
    @DisplayName("searchByName: case-insensitive 'contains', ordered by name")
    void searchByName() {
        assertThat(catalog.searchByName("NOTE")).extracting("name").containsExactly("Notebook A4", "Notebook A5");
    }

    @Test
    @DisplayName("searchByName with no match is an empty list")
    void searchNoMatch() {
        assertThat(catalog.searchByName("zzz")).isEmpty();
    }
}
