package shopflow.api.w12;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.persistence.EntityManagerFactory;

/**
 * Base class of all week 12 tests: one real PostgreSQL in Docker (started once for the whole run), wired into Spring
 * Boot by @ServiceConnection, with Flyway migrating it. Every test starts with empty tables.
 * Needs Docker running.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
abstract class AbstractPostgresTest {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void cleanDatabase() {
        for (String table : List.of("cart_item", "cart", "order_item", "customer_order", "product")) {
            Boolean exists = jdbc.queryForObject("select to_regclass(?) is not null", Boolean.class, table);
            if (Boolean.TRUE.equals(exists)) {
                jdbc.execute("truncate table " + table + " restart identity cascade");
            }
        }
    }

    /** Hibernate's counters; statistics().clear() then run the code, then read getPrepareStatementCount(). */
    protected Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    protected long count(String table) {
        Long n = jdbc.queryForObject("select count(*) from " + table, Long.class);
        return n == null ? 0 : n;
    }
}
