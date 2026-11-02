package shopflow.api.w12;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Week 12 application: a full Spring Boot app with Postgres (JPA + Flyway). Needs a running database; the tests start one with Testcontainers. */
@SpringBootApplication
public class W12App {

    public static void main(String[] args) {
        SpringApplication.run(W12App.class, args);
    }
}
