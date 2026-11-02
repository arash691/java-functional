package shopflow.api.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lab helper: a normal {@code @SpringBootApplication} that switches off the database auto-configuration.
 * Weeks 10 and 11 run without a database; week 12 uses a plain {@code @SpringBootApplication}.
 * (The module has the JPA and Flyway starters on the classpath for week 12, so without this
 * Boot would insist on a DataSource.)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootApplication(excludeName = {
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
        "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
public @interface NoDbApp {
}
