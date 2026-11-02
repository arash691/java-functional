package shopflow.api.w12;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import shopflow.api.w12.catalog.ProductCatalog;

/** EX-12-02: a schema change = a new Flyway migration + a new entity field. (Do EX-12-01 first: it reads back through findBySku.) */
@Tag("w12")
@DisplayName("EX-12-02 products have a description")
class Ex2ProductDescriptionTest extends AbstractPostgresTest {

    @Autowired
    ProductCatalog catalog;

    @Test
    @DisplayName("the product table has a nullable 'description' column (write V2__product_description.sql)")
    void columnExists() {
        Long columns = jdbc.queryForObject(
                "select count(*) from information_schema.columns where table_name = 'product' and column_name = 'description'",
                Long.class);
        assertThat(columns).as("description column in table product").isEqualTo(1L);
    }

    @Test
    @DisplayName("create stores the description and findBySku returns it")
    void roundTrip() {
        var created = catalog.create("INK-1", "Ink", "Blue, 50 ml", new BigDecimal("4.50"));

        assertThat(created.id()).isNotNull();
        assertThat(created.description()).isEqualTo("Blue, 50 ml");
        assertThat(catalog.findBySku("INK-1")).get().extracting("description").isEqualTo("Blue, 50 ml");
    }

    @Test
    @DisplayName("the description may be null")
    void nullDescription() {
        var created = catalog.create("INK-2", "Red ink", null, new BigDecimal("4.50"));
        assertThat(created.description()).isNull();
        assertThat(catalog.findBySku("INK-2")).isPresent();
    }
}
